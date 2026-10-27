from uuid import UUID
from fastapi import APIRouter, Depends, HTTPException, Query
from .database import get_supabase, verify_api_key
from .matching_engine import match_material_with_requirements

router = APIRouter(
    prefix="/materials",
    tags=["Intelligent Matching Engine"],
    dependencies=[Depends(verify_api_key)],
)


@router.get("/{material_id}/matches")
def get_matches_for_material(
    material_id: UUID,
    max_distance_km: float = Query(
        default=300.0, gt=0, le=2000, description="Maximum straight-line search radius in km"
    ),
):
    supabase = get_supabase()

    try:
        mat_res = (
            supabase.table("materials")
            .select("*")
            .eq("id", str(material_id))
            .execute()
        )
    except Exception as exc:
        raise HTTPException(
            status_code=500, detail="Failed to fetch material from database."
        ) from exc

    if not mat_res.data:
        raise HTTPException(
            status_code=404, detail=f"Material '{material_id}' not found."
        )

    material = mat_res.data[0]

    try:
        req_res = supabase.table("buyer_requirements").select("*").execute()
    except Exception as exc:
        raise HTTPException(
            status_code=500, detail="Failed to fetch buyer requirements from database."
        ) from exc

    requirements = req_res.data or []

    evaluation = match_material_with_requirements(
        material=material,
        requirements=requirements,
        max_distance_km=max_distance_km,
    )

    is_synthetic = bool(material.get("is_synthetic"))

    return {
        "material_id": material["id"],
        "seller_name": material["seller_name"],
        "material_name": material["material_name"],
        "available_quantity_tonnes": material["quantity_tonnes"],
        "seller_location": {
            "city": material["city"],
            "state": material["state"],
            "latitude": material["latitude"],
            "longitude": material["longitude"],
        },
        "max_distance_km": max_distance_km,
        "total_matches": len(evaluation["matches"]),
        "total_rejected": len(evaluation["rejected_candidates"]),
        "matches": evaluation["matches"],
        "rejected_candidates": evaluation["rejected_candidates"],
        "data_type": "synthetic_demo" if is_synthetic else "user_submitted",
        "disclaimer": (
            "Scores and matches are preliminary decision-support estimates based on "
            "user-declared values and straight-line Haversine distance. They do not "
            "certify material safety, batch quality, or regulatory compliance."
        ),
    }