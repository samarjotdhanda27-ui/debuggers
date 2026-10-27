from typing import Any, Dict, List, Optional
from uuid import UUID

from fastapi import APIRouter, Depends, HTTPException, status
from pydantic import BaseModel, Field, model_validator

from .database import get_supabase, verify_api_key


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
    material_properties: Dict[str, Any] = Field(default_factory=dict)
    available_documents: List[str] = Field(default_factory=list)
    is_synthetic: bool = False


class MaterialUpdate(BaseModel):
    """
    Partial updates for an existing seller material listing.

    Only supplied fields are updated.
    The listing ID and creation timestamp cannot be modified.
    """

    seller_name: Optional[str] = Field(default=None, min_length=2)
    material_name: Optional[str] = Field(default=None, min_length=2)
    source_industry: Optional[str] = Field(default=None, min_length=2)
    quantity_tonnes: Optional[float] = Field(default=None, gt=0)
    city: Optional[str] = Field(default=None, min_length=2)
    state: Optional[str] = Field(default=None, min_length=2)
    latitude: Optional[float] = Field(default=None, ge=-90, le=90)
    longitude: Optional[float] = Field(default=None, ge=-180, le=180)
    material_properties: Optional[Dict[str, Any]] = None
    available_documents: Optional[List[str]] = None

    @model_validator(mode="after")
    def validate_non_null_updates(self):
        for name in self.model_fields_set:
            if getattr(self, name) is None:
                raise ValueError(
                    f"Field '{name}' cannot be explicitly set to null."
                )
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
    try:
        supabase = get_supabase()
        response = (
            supabase.table("materials")
            .insert(payload.model_dump())
            .execute()
        )

        return {
            "message": "Material listing created successfully",
            "material": response.data[0],
        }

    except Exception as exc:
        raise HTTPException(
            status_code=500,
            detail="Failed to save material listing.",
        ) from exc


@router.get("/materials")
def list_materials():
    try:
        supabase = get_supabase()
        response = (
            supabase.table("materials")
            .select("*")
            .order("created_at", desc=True)
            .execute()
        )

        return {
            "count": len(response.data),
            "materials": response.data,
        }

    except Exception as exc:
        raise HTTPException(
            status_code=500,
            detail="Failed to load material listings.",
        ) from exc


@router.patch("/materials/{material_id}")
def update_material(material_id: UUID, payload: MaterialUpdate):
    """
    Update a stored material listing.

    The matching engine reads material data from Supabase for each request,
    so subsequent matching calls automatically use the updated properties.
    """

    updates = payload.model_dump(exclude_unset=True)

    if not updates:
        raise HTTPException(
            status_code=400,
            detail="No fields provided for update.",
        )

    # Prevent the existing demo endpoint from changing non-demo listings.
    try:
        supabase = get_supabase()

        existing = (
            supabase.table("materials")
            .select("id,is_synthetic")
            .eq("id", str(material_id))
            .execute()
        )

        if not existing.data:
            raise HTTPException(
                status_code=404,
                detail="Material listing not found.",
            )

        if not existing.data[0].get("is_synthetic"):
            raise HTTPException(
                status_code=403,
                detail=(
                    "Material editing is restricted to synthetic demo "
                    "records until seller authentication is implemented."
                ),
            )

        result = (
            supabase.table("materials")
            .update(updates)
            .eq("id", str(material_id))
            .eq("is_synthetic", True)
            .execute()
        )

        if not result.data:
            raise HTTPException(
                status_code=404,
                detail="Material listing not found or not editable.",
            )

        return {
            "message": "Material listing updated successfully",
            "material": result.data[0],
            "matching_refresh_required": True,
            "next_step": (
                f"GET /materials/{material_id}/matches"
            ),
        }

    except HTTPException:
        raise

    except Exception as exc:
        raise HTTPException(
            status_code=500,
            detail="Failed to update material listing.",
        ) from exc


@router.post("/requirements", status_code=status.HTTP_201_CREATED)
def create_requirement(payload: RequirementCreate):
    try:
        supabase = get_supabase()

        response = (
            supabase.table("buyer_requirements")
            .insert(payload.model_dump())
            .execute()
        )

        return {
            "message": "Buyer requirement created successfully",
            "requirement": response.data[0],
        }

    except Exception as exc:
        raise HTTPException(
            status_code=500,
            detail="Failed to save buyer requirement.",
        ) from exc


@router.get("/requirements")
def list_requirements():
    try:
        supabase = get_supabase()

        response = (
            supabase.table("buyer_requirements")
            .select("*")
            .order("created_at", desc=True)
            .execute()
        )

        return {
            "count": len(response.data),
            "requirements": response.data,
        }

    except Exception as exc:
        raise HTTPException(
            status_code=500,
            detail="Failed to load buyer requirements.",
        ) from exc