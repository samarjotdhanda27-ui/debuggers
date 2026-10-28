from fastapi import APIRouter, Depends, HTTPException, Query
from .database import get_supabase, verify_api_key
from .mongodb import get_mongodb, check_mongodb_connection
from .matching_engine import match_material_with_requirements

router = APIRouter(
    prefix="/materials",
    tags=["Intelligent Matching Engine"],
    dependencies=[Depends(verify_api_key)],
)


@router.get("/{material_id}/matches")
def get_matches_for_material(
    material_id: str,
    max_distance_km: float = Query(
        default=300.0, gt=0, le=2000, description="Maximum straight-line search radius in km"
    ),
):
    material = None

    # 1. Fetch material from Supabase
    try:
        supabase = get_supabase()
        mat_res = (
            supabase.table("materials")
            .select("*")
            .eq("id", str(material_id))
            .execute()
        )
        if mat_res.data:
            material = mat_res.data[0]
    except Exception as exc:
        print(f"[Warning] Supabase fetch material for matching: {exc}")

    # 2. If not found in Supabase, fetch from MongoDB
    if not material and check_mongodb_connection():
        try:
            db = get_mongodb()
            doc = db["materials"].find_one({"id": str(material_id)})
            if doc:
                doc["id"] = str(doc.get("id", doc.get("_id")))
                doc.pop("_id", None)
                material = doc
        except Exception as exc:
            print(f"[Warning] MongoDB fetch material for matching: {exc}")

    if not material:
        raise HTTPException(
            status_code=404, detail=f"Material '{material_id}' not found."
        )

    # 3. Fetch buyer requirements
    requirements = []
    try:
        supabase = get_supabase()
        req_res = supabase.table("buyer_requirements").select("*").execute()
        if req_res.data:
            requirements = req_res.data
    except Exception as exc:
        print(f"[Warning] Supabase fetch requirements for matching: {exc}")

    if not requirements and check_mongodb_connection():
        try:
            db = get_mongodb()
            cursor = db["buyer_requirements"].find().limit(100)
            for doc in cursor:
                doc["id"] = str(doc.get("id", doc.get("_id")))
                doc.pop("_id", None)
                requirements.append(doc)
        except Exception as exc:
            print(f"[Warning] MongoDB fetch requirements for matching: {exc}")

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
