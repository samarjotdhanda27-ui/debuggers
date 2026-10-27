from fastapi import APIRouter, Depends, HTTPException
from fastapi.responses import StreamingResponse
from sqlalchemy.orm import Session
import sys
import os
sys.path.append(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))

from database import get_db
from models.passport import MaterialPassport
from schemas.passport import PassportCreate, PassportResponse
from utils.qr_generator import generate_qr_image

router = APIRouter(prefix="/passport", tags=["Material Passport"])


@router.post("/", response_model=PassportResponse)
def create_passport(data: PassportCreate, db: Session = Depends(get_db)):
    existing = db.query(MaterialPassport).filter(MaterialPassport.batch_id == data.batch_id).first()
    if existing:
        raise HTTPException(status_code=400, detail="Batch ID already exists")

    completeness = "Complete" if len(data.reports) >= 2 else "Partial"

    passport = MaterialPassport(
        material_name=data.material_name,
        batch_id=data.batch_id,
        source_factory=data.source_factory,
        quantity=data.quantity,
        properties=data.properties,
        reports=data.reports,
        docs_completeness=completeness,
        history=[]
    )
    db.add(passport)
    db.commit()
    db.refresh(passport)
    return passport


@router.get("/{passport_id}", response_model=PassportResponse)
def get_passport(passport_id: str, db: Session = Depends(get_db)):
    passport = db.query(MaterialPassport).filter(MaterialPassport.id == passport_id).first()
    if not passport:
        raise HTTPException(status_code=404, detail="Passport not found")
    return passport


@router.get("/", response_model=list[PassportResponse])
def list_passports(db: Session = Depends(get_db)):
    return db.query(MaterialPassport).all()


# ---------- NEW: QR CODE ENDPOINT ----------
@router.get("/{passport_id}/qr")
def get_passport_qr(passport_id: str, db: Session = Depends(get_db)):
    # First check if the passport actually exists
    passport = db.query(MaterialPassport).filter(MaterialPassport.id == passport_id).first()
    if not passport:
        raise HTTPException(status_code=404, detail="Passport not found")

    # The QR code will encode a URL that opens this passport's record
   passport_url = f"https://reloop-ui.onrender.com/passport/{passport_id}"

    qr_image = generate_qr_image(passport_url)

    return StreamingResponse(qr_image, media_type="image/png")