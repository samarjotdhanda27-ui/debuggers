from fastapi import APIRouter
from .geo_matching import load_json

router = APIRouter(prefix="/pathways", tags=["Reuse Pathways"])


@router.get("/{material_name}")
def get_reuse_pathways(material_name: str):
    name_clean = material_name.strip().lower()

    # Load base json for reference
    try:
        data = load_json("reuse_pathways.json")
    except Exception:
        data = {"disclaimer": "Industrial symbiosis reuse recommendations based on standard specifications."}

    if "fly" in name_clean or "ash" in name_clean:
        pathways = []
        for p in data.get("reuse_pathways", []):
            pathways.append({
                "id": p["id"],
                "buyer_industry": p["buyer_industry"],
                "application": p["application"],
                "relevant_properties": p["relevant_properties"],
                "reference_standard": p["reference_standard"],
                "verification_required": p.get("verification_required", True),
                "evidence_sources": [
                    ev.get("source_id", "CEA2026") for ev in p.get("evidence", [])
                ]
            })
        return {
            "material": "Fly Ash",
            "source_industry": "Thermal Power Generation",
            "total_pathways": len(pathways),
            "pathways": pathways,
            "disclaimer": data.get("disclaimer", "")
        }

    elif "sand" in name_clean or "foundry" in name_clean or "silica" in name_clean:
        return {
            "material": material_name,
            "source_industry": "Foundry & Casting",
            "total_pathways": 2,
            "pathways": [
                {
                    "id": "RP101",
                    "buyer_industry": "Construction Aggregates",
                    "application": "Fine Aggregate in Concrete & Mortar",
                    "relevant_properties": ["grain_fineness", "clay_content", "leachable_phenols"],
                    "reference_standard": "IS 383",
                    "verification_required": True,
                    "evidence_sources": ["CPCB-FS2025", "BIS2024"]
                },
                {
                    "id": "RP102",
                    "buyer_industry": "Brick Manufacturing",
                    "application": "Clay Brick Additive & Pavers",
                    "relevant_properties": ["particle_size", "loss_on_ignition"],
                    "reference_standard": "IS 1077",
                    "verification_required": True,
                    "evidence_sources": ["BIS2024"]
                }
            ],
            "disclaimer": data.get("disclaimer", "")
        }

    elif "steel" in name_clean or "scrap" in name_clean or "metal" in name_clean or "iron" in name_clean:
        return {
            "material": material_name,
            "source_industry": "Steel & Metallurgy",
            "total_pathways": 1,
            "pathways": [
                {
                    "id": "RP201",
                    "buyer_industry": "Secondary Steel Metallurgy",
                    "application": "Electric Arc Furnace Charge",
                    "relevant_properties": ["fe_purity", "bulk_density", "oil_contaminants"],
                    "reference_standard": "IS 2549",
                    "verification_required": True,
                    "evidence_sources": ["MISTEEL2025"]
                }
            ],
            "disclaimer": data.get("disclaimer", "")
        }

    elif "plastic" in name_clean or "hdpe" in name_clean or "polymer" in name_clean:
        return {
            "material": material_name,
            "source_industry": "Plastics & Polymers",
            "total_pathways": 1,
            "pathways": [
                {
                    "id": "RP301",
                    "buyer_industry": "Recycled Polymer Extrusion",
                    "application": "Non-Pressure Pipes & Conduits",
                    "relevant_properties": ["melt_flow_index", "density", "ash_content"],
                    "reference_standard": "IS 4984 / IS 14535",
                    "verification_required": True,
                    "evidence_sources": ["CIPET2025"]
                }
            ],
            "disclaimer": data.get("disclaimer", "")
        }

    elif "paper" in name_clean or "cardboard" in name_clean or "occ" in name_clean:
        return {
            "material": material_name,
            "source_industry": "Pulp & Paper",
            "total_pathways": 1,
            "pathways": [
                {
                    "id": "RP401",
                    "buyer_industry": "Secondary Fiber Packaging",
                    "application": "Kraft Fluting & Linerboard",
                    "relevant_properties": ["burst_factor", "moisture_content", "outthrows"],
                    "reference_standard": "IS 1397",
                    "verification_required": True,
                    "evidence_sources": ["CPPRI2024"]
                }
            ],
            "disclaimer": data.get("disclaimer", "")
        }

    else:
        return {
            "material": material_name,
            "source_industry": "Industrial Circular Manufacturing",
            "total_pathways": 1,
            "pathways": [
                {
                    "id": "RP999",
                    "buyer_industry": "Circular Manufacturing",
                    "application": "Direct Secondary Feedstock",
                    "relevant_properties": ["composition", "moisture", "contaminant_screening"],
                    "reference_standard": "IS Standards Applicable",
                    "verification_required": True,
                    "evidence_sources": ["ReLoop-Circular-Engine"]
                }
            ],
            "disclaimer": data.get("disclaimer", "")
        }
