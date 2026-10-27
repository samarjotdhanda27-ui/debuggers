from typing import Any, Dict, List
from fastapi import APIRouter, Depends, HTTPException, status
from pydantic import BaseModel, Field
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
            supabase.table("materials").insert(payload.model_dump()).execute()
        )
        return {
            "message": "Material listing created successfully",
            "material": response.data[0],
        }
    except Exception as exc:
        raise HTTPException(
            status_code=500, detail="Failed to save material listing."
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
        return {"count": len(response.data), "materials": response.data}
    except Exception as exc:
        raise HTTPException(
            status_code=500, detail="Failed to load material listings."
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
            status_code=500, detail="Failed to save buyer requirement."
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
        return {"count": len(response.data), "requirements": response.data}
    except Exception as exc:
        raise HTTPException(
            status_code=500, detail="Failed to load buyer requirements."
        ) from exc