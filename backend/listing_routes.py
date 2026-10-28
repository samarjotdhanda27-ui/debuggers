import uuid
from datetime import datetime
from typing import Any, Dict, List, Optional
from uuid import UUID

from fastapi import APIRouter, Depends, HTTPException, Query, status
from pydantic import BaseModel, Field, model_validator

from .database import get_supabase, verify_api_key
from .mongodb import get_mongodb, check_mongodb_connection


router = APIRouter(
    tags=["Marketplace Listings"],
    dependencies=[Depends(verify_api_key)],
)


class MaterialCreate(BaseModel):
    seller_name: str = Field(..., min_length=2)
    material_name: str = Field(..., min_length=2)
    source_industry: str = Field(..., min_length=2)
    quantity_tonnes: float = Field(..., gt=0)
    city: str = Field(..., min_length=2)
    state: str = Field(..., min_length=2)
    latitude: float = Field(..., ge=-90, le=90)
    longitude: float = Field(..., ge=-180, le=180)
    properties: Optional[Dict[str, Any]] = Field(default_factory=dict)
    material_properties: Optional[Dict[str, Any]] = Field(default_factory=dict)
    available_documents: List[str] = Field(default_factory=list)
    is_synthetic: bool = False

    @model_validator(mode="after")
    def sync_properties_fields(self):
        # Sync properties and material_properties so both Android and web work identically
        if self.properties and not self.material_properties:
            self.material_properties = self.properties
        elif self.material_properties and not self.properties:
            self.properties = self.material_properties
        return self


class MaterialUpdate(BaseModel):
    seller_name: Optional[str] = Field(default=None, min_length=2)
    material_name: Optional[str] = Field(default=None, min_length=2)
    source_industry: Optional[str] = Field(default=None, min_length=2)
    quantity_tonnes: Optional[float] = Field(default=None, gt=0)
    city: Optional[str] = Field(default=None, min_length=2)
    state: Optional[str] = Field(default=None, min_length=2)
    latitude: Optional[float] = Field(default=None, ge=-90, le=90)
    longitude: Optional[float] = Field(default=None, ge=-180, le=180)
    properties: Optional[Dict[str, Any]] = None
    material_properties: Optional[Dict[str, Any]] = None
    available_documents: Optional[List[str]] = None

    @model_validator(mode="after")
    def validate_non_null_updates(self):
        for name in self.model_fields_set:
            if getattr(self, name) is None:
                raise ValueError(
                    f"Field '{name}' cannot be explicitly set to null."
                )
        if self.properties and not self.material_properties:
            self.material_properties = self.properties
        elif self.material_properties and not self.properties:
            self.properties = self.material_properties
        return self


class RequirementCreate(BaseModel):
    buyer_name: str = Field(..., min_length=2)
    buyer_industry: str = Field(..., min_length=2)
    required_material: str = Field(..., min_length=2)
    required_quantity_tonnes: float = Field(..., gt=0)
    city: str = Field(..., min_length=2)
    state: str = Field(..., min_length=2)
    latitude: float = Field(..., ge=-90, le=90)
    longitude: float = Field(..., ge=-180, le=180)
    required_properties: Dict[str, Any] = Field(default_factory=dict)
    required_documents: List[str] = Field(default_factory=list)
    is_synthetic: bool = False


@router.post("/materials", status_code=status.HTTP_201_CREATED)
def create_material(payload: MaterialCreate):
    material_data = payload.model_dump()
    material_id = str(uuid.uuid4())
    material_data["id"] = material_id
    material_data["created_at"] = datetime.utcnow().strftime("%Y-%m-%d")

    created_material = None
    saved_supabase = False
    saved_mongo = False

    # 1. Save to Supabase
    try:
        supabase = get_supabase()
        # Supabase schema expects material_properties
        supa_payload = dict(material_data)
        if "properties" in supa_payload and "material_properties" not in supa_payload:
            supa_payload["material_properties"] = supa_payload["properties"]
        response = (
            supabase.table("materials")
            .insert(supa_payload)
            .execute()
        )
        if response.data:
            created_material = response.data[0]
            saved_supabase = True
    except Exception as exc:
        print(f"[Warning] Supabase material insert: {exc}")

    # 2. Save to MongoDB Atlas
    try:
        if check_mongodb_connection():
            db = get_mongodb()
            mongo_payload = dict(material_data)
            mongo_payload["_id"] = material_id
            db["materials"].insert_one(mongo_payload)
            mongo_payload.pop("_id", None)
            if not created_material:
                created_material = mongo_payload
            saved_mongo = True
    except Exception as exc:
        print(f"[Warning] MongoDB material insert: {exc}")

    if not created_material and not (saved_supabase or saved_mongo):
        raise HTTPException(
            status_code=500,
            detail="Failed to save material listing to both database engines.",
        )

    return {
        "message": "Material listing created successfully",
        "material": created_material or material_data,
        "sync_status": {
            "supabase": saved_supabase,
            "mongodb": saved_mongo
        }
    }


@router.get("/materials")
def list_materials(
    q: Optional[str] = Query(None, description="Search query"),
    category: Optional[str] = Query(None, description="Category filter")
):
    materials = []

    # 1. Try Supabase
    try:
        supabase = get_supabase()
        response = (
            supabase.table("materials")
            .select("*")
            .order("created_at", desc=True)
            .execute()
        )
        if response.data:
            materials = response.data
    except Exception as exc:
        print(f"[Warning] Supabase fetch materials failed: {exc}")

    # 2. If Supabase returned empty or failed, fetch from MongoDB
    if not materials:
        try:
            if check_mongodb_connection():
                db = get_mongodb()
                cursor = db["materials"].find().sort("created_at", -1).limit(100)
                for doc in cursor:
                    doc["id"] = str(doc.get("id", doc.get("_id")))
                    doc.pop("_id", None)
                    materials.append(doc)
        except Exception as exc:
            print(f"[Warning] MongoDB fetch materials failed: {exc}")

    # Filter by q if present
    if q:
        query_lower = q.lower()
        materials = [
            m for m in materials
            if query_lower in m.get("material_name", "").lower()
            or query_lower in m.get("seller_name", "").lower()
            or query_lower in m.get("source_industry", "").lower()
        ]

    # Filter by category if present
    if category and category != "All":
        materials = [
            m for m in materials
            if (m.get("properties", {}).get("category") == category)
            or (m.get("material_properties", {}).get("category") == category)
        ]

    return {
        "count": len(materials),
        "materials": materials,
    }


@router.get("/materials/{material_id}")
def get_material(material_id: str):
    # Try Supabase
    try:
        supabase = get_supabase()
        response = supabase.table("materials").select("*").eq("id", material_id).execute()
        if response.data:
            return response.data[0]
    except Exception:
        pass

    # Try MongoDB
    try:
        if check_mongodb_connection():
            db = get_mongodb()
            doc = db["materials"].find_one({"id": material_id})
            if doc:
                doc["id"] = str(doc.get("id", doc.get("_id")))
                doc.pop("_id", None)
                return doc
    except Exception:
        pass

    raise HTTPException(status_code=404, detail="Material listing not found.")


@router.patch("/materials/{material_id}")
def update_material(material_id: UUID, payload: MaterialUpdate):
    updates = payload.model_dump(exclude_unset=True)
    if not updates:
        raise HTTPException(status_code=400, detail="No fields provided for update.")

    try:
        supabase = get_supabase()
        existing = (
            supabase.table("materials")
            .select("id,is_synthetic")
            .eq("id", str(material_id))
            .execute()
        )

        if not existing.data:
            raise HTTPException(status_code=404, detail="Material listing not found.")

        result = (
            supabase.table("materials")
            .update(updates)
            .eq("id", str(material_id))
            .execute()
        )

        # Sync update to MongoDB
        try:
            if check_mongodb_connection():
                db = get_mongodb()
                db["materials"].update_one({"id": str(material_id)}, {"$set": updates})
        except Exception:
            pass

        return {
            "message": "Material listing updated successfully",
            "material": result.data[0] if result.data else updates,
            "matching_refresh_required": True,
            "next_step": f"GET /materials/{material_id}/matches",
        }

    except HTTPException:
        raise
    except Exception as exc:
        raise HTTPException(status_code=500, detail="Failed to update material listing.") from exc


@router.post("/requirements", status_code=status.HTTP_201_CREATED)
def create_requirement(payload: RequirementCreate):
    req_data = payload.model_dump()
    req_id = str(uuid.uuid4())
    req_data["id"] = req_id
    req_data["created_at"] = datetime.utcnow().strftime("%Y-%m-%d")

    created_requirement = None
    saved_supabase = False
    saved_mongo = False

    try:
        supabase = get_supabase()
        response = (
            supabase.table("buyer_requirements")
            .insert(req_data)
            .execute()
        )
        if response.data:
            created_requirement = response.data[0]
            saved_supabase = True
    except Exception as exc:
        print(f"[Warning] Supabase requirement insert: {exc}")

    try:
        if check_mongodb_connection():
            db = get_mongodb()
            mongo_payload = dict(req_data)
            mongo_payload["_id"] = req_id
            db["buyer_requirements"].insert_one(mongo_payload)
            mongo_payload.pop("_id", None)
            if not created_requirement:
                created_requirement = mongo_payload
            saved_mongo = True
    except Exception as exc:
        print(f"[Warning] MongoDB requirement insert: {exc}")

    if not created_requirement and not (saved_supabase or saved_mongo):
        raise HTTPException(
            status_code=500,
            detail="Failed to save buyer requirement.",
        )

    return {
        "message": "Buyer requirement created successfully",
        "requirement": created_requirement or req_data,
        "sync_status": {
            "supabase": saved_supabase,
            "mongodb": saved_mongo
        }
    }


@router.get("/requirements")
def list_requirements():
    requirements = []

    try:
        supabase = get_supabase()
        response = (
            supabase.table("buyer_requirements")
            .select("*")
            .order("created_at", desc=True)
            .execute()
        )
        if response.data:
            requirements = response.data
    except Exception as exc:
        print(f"[Warning] Supabase fetch requirements failed: {exc}")

    if not requirements:
        try:
            if check_mongodb_connection():
                db = get_mongodb()
                cursor = db["buyer_requirements"].find().sort("created_at", -1).limit(100)
                for doc in cursor:
                    doc["id"] = str(doc.get("id", doc.get("_id")))
                    doc.pop("_id", None)
                    requirements.append(doc)
        except Exception as exc:
            print(f"[Warning] MongoDB fetch requirements failed: {exc}")

    return {
        "count": len(requirements),
        "requirements": requirements,
    }
