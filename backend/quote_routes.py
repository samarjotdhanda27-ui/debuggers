import uuid
from datetime import datetime
from typing import Optional
from fastapi import APIRouter, HTTPException, status
from pydantic import BaseModel

from .mongodb import get_mongodb, check_mongodb_connection

router = APIRouter(prefix="/quotes", tags=["Quotation & RFQ Management"])


class QuoteCreate(BaseModel):
    material_id: str
    material_name: str
    seller_name: str
    buyer_name: str
    buyer_contact: str
    requested_quantity_tonnes: float
    delivery_city: str
    message: Optional[str] = ""


@router.post("/", status_code=status.HTTP_201_CREATED)
def submit_quote(quote: QuoteCreate):
    quote_data = quote.model_dump()
    quote_id = f"RFQ-{uuid.uuid4().hex[:8].upper()}"
    quote_data["id"] = quote_id
    quote_data["timestamp"] = datetime.utcnow().strftime("%d %b %Y, %H:%M IST")
    quote_data["status"] = "Submitted · Pending Seller Review"

    saved_mongo = False
    try:
        if check_mongodb_connection():
            db = get_mongodb()
            mongo_payload = dict(quote_data)
            mongo_payload["_id"] = quote_id
            db["quotes"].insert_one(mongo_payload)
            saved_mongo = True
    except Exception as exc:
        print(f"[Warning] MongoDB quote insert error: {exc}")

    return {
        "success": True,
        "message": "Quotation request delivered to seller",
        "quote": quote_data,
        "saved_to_mongodb": saved_mongo
    }


@router.get("/")
def list_quotes():
    quotes = []
    try:
        if check_mongodb_connection():
            db = get_mongodb()
            cursor = db["quotes"].find().sort("timestamp", -1).limit(50)
            for doc in cursor:
                doc["id"] = str(doc.get("id", doc.get("_id")))
                doc.pop("_id", None)
                quotes.append(doc)
    except Exception as exc:
        print(f"[Warning] MongoDB fetch quotes error: {exc}")

    return {
        "count": len(quotes),
        "quotes": quotes
    }
