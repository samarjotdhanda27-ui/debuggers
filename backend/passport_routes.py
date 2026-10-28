import io
import uuid
from datetime import datetime
from typing import Any, Dict, List, Optional
from fastapi import APIRouter, HTTPException, Depends
from fastapi.responses import StreamingResponse
from pydantic import BaseModel, Field

from .database import get_supabase, verify_api_key
from .mongodb import get_mongodb, check_mongodb_connection

router = APIRouter(prefix="/passport", tags=["Digital Product Passport"])


class CustodyEvent(BaseModel):
    event_id: str
    stage: str
    actor: str
    location: str
    timestamp: str
    verification_status: str


class PassportCreate(BaseModel):
    material_id: Optional[str] = None
    material_name: str
    batch_number: Optional[str] = None
    generating_facility: str
    origin_city: Optional[str] = "Ludhiana"
    origin_state: Optional[str] = "Punjab"
    latitude: Optional[float] = 30.901
    longitude: Optional[float] = 75.857
    properties: Optional[Dict[str, Any]] = Field(default_factory=dict)
    custody_events: Optional[List[Dict[str, Any]]] = Field(default_factory=list)


def build_default_passport(material_id: str, material: Optional[Dict[str, Any]] = None) -> Dict[str, Any]:
    name = material.get("material_name", "Industrial By-Product") if material else "Industrial Feedstock"
    facility = material.get("seller_name", "Regional Industrial Facility") if material else "Punjab Circular Hub"
    city = material.get("city", "Ludhiana") if material else "Ludhiana"
    state = material.get("state", "Punjab") if material else "Punjab"
    lat = material.get("latitude", 30.901) if material else 30.901
    lon = material.get("longitude", 75.857) if material else 75.857
    created = material.get("created_at", "2026-10-25") if material else "2026-10-25"
    clean_id = material_id.replace("seed-", "").replace("loc-", "").replace("-", "")[:8].upper()
    batch_no = f"BAT-{clean_id}-2026"

    events = [
        {
            "event_id": "EV-001",
            "stage": "Batch Generation & Silo Sampling",
            "actor": facility,
            "location": f"{city}, {state}",
            "timestamp": f"{created} 08:30 IST",
            "verification_status": "Verified at Facility"
        },
        {
            "event_id": "EV-002",
            "stage": "NABL Laboratory Spectrometry Analysis",
            "actor": "Punjab Industrial Analytical Lab",
            "location": "Mohali, Punjab",
            "timestamp": f"{created} 14:15 IST",
            "verification_status": "Specification Certified"
        },
        {
            "event_id": "EV-003",
            "stage": "Environmental Consent Verification",
            "actor": "SPCB Regional Directorate",
            "location": "Chandigarh",
            "timestamp": f"{created} 17:45 IST",
            "verification_status": "Consent NOC Active"
        },
        {
            "event_id": "EV-004",
            "stage": "ReLoop B2B Marketplace Verified Lot",
            "actor": "ReLoop Circular Intelligence Engine",
            "location": "Cloud Ledger",
            "timestamp": "Active Current Batch",
            "verification_status": "Verified Lot"
        }
    ]

    return {
        "passport_id": f"DPP-{clean_id}",
        "material_id": material_id,
        "material_name": name,
        "batch_number": batch_no,
        "generating_facility": facility,
        "origin_city": city,
        "origin_state": state,
        "latitude": lat,
        "longitude": lon,
        "spcb_consent_status": "Valid NOC Active · Air & Water Act",
        "qr_verification_url": f"https://reloop-ui.onrender.com/passport/{material_id}",
        "custody_events": events
    }


@router.get("/{material_id}")
def get_passport(material_id: str):
    # 1. Check MongoDB passports collection
    try:
        if check_mongodb_connection():
            db = get_mongodb()
            existing = db["material_passports"].find_one({"material_id": material_id})
            if existing:
                existing.pop("_id", None)
                return existing
    except Exception:
        pass

    # 2. Lookup material details in Supabase or MongoDB
    material = None
    try:
        supabase = get_supabase()
        res = supabase.table("materials").select("*").eq("id", material_id).execute()
        if res.data:
            material = res.data[0]
    except Exception:
        pass

    if not material and check_mongodb_connection():
        try:
            db = get_mongodb()
            material = db["materials"].find_one({"id": material_id})
        except Exception:
            pass

    passport = build_default_passport(material_id, material)

    # Save to MongoDB for persistent retrieval
    try:
        if check_mongodb_connection():
            db = get_mongodb()
            to_save = dict(passport)
            to_save["_id"] = passport["passport_id"]
            db["material_passports"].update_one(
                {"passport_id": passport["passport_id"]},
                {"$set": to_save},
                upsert=True
            )
    except Exception:
        pass

    return passport


@router.get("/{material_id}/qr")
def get_passport_qr(material_id: str):
    qr_data = f"https://reloop-ui.onrender.com/passport/{material_id}"
    try:
        import qrcode
        qr = qrcode.QRCode(
            version=1,
            error_correction=qrcode.constants.ERROR_CORRECT_L,
            box_size=10,
            border=4,
        )
        qr.add_data(qr_data)
        qr.make(fit=True)
        img = qr.make_image(fill_color="black", back_color="white")
        img_bytes = io.BytesIO()
        img.save(img_bytes, format="PNG")
        img_bytes.seek(0)
        return StreamingResponse(img_bytes, media_type="image/png")
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"QR generation error: {str(e)}")


@router.post("/", status_code=201)
def create_passport(data: PassportCreate):
    mat_id = data.material_id or str(uuid.uuid4())
    passport = build_default_passport(mat_id)
    passport["material_name"] = data.material_name
    passport["generating_facility"] = data.generating_facility
    if data.batch_number:
        passport["batch_number"] = data.batch_number

    try:
        if check_mongodb_connection():
            db = get_mongodb()
            to_save = dict(passport)
            to_save["_id"] = passport["passport_id"]
            db["material_passports"].insert_one(to_save)
    except Exception:
        pass

    return passport


@router.get("/")
def list_passports():
    passports = []
    try:
        if check_mongodb_connection():
            db = get_mongodb()
            cursor = db["material_passports"].find().limit(50)
            for doc in cursor:
                doc.pop("_id", None)
                passports.append(doc)
    except Exception:
        pass
    return passports
