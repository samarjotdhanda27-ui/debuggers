from typing import Optional

import cloudinary.uploader
from fastapi import APIRouter, Depends, File, Form, HTTPException, UploadFile, status

from .cloudinary_config import check_cloudinary_connection, configure_cloudinary
from .database import verify_api_key
from .mongodb import list_upload_records, save_upload_record

router = APIRouter(
    prefix="/media",
    tags=["Media Uploads"],
    dependencies=[Depends(verify_api_key)],
)

MAX_FILE_BYTES = 10 * 1024 * 1024


@router.post("/upload", status_code=status.HTTP_201_CREATED)
async def upload_media(
    file: UploadFile = File(...),
    listing_id: Optional[str] = Form(default=None),
    folder: str = Form(default="reloop"),
):
    if not check_cloudinary_connection():
        raise HTTPException(
            status_code=503,
            detail="Cloudinary is not configured on this server.",
        )

    contents = await file.read()
    if not contents:
        raise HTTPException(status_code=400, detail="Uploaded file is empty.")

    if len(contents) > MAX_FILE_BYTES:
        raise HTTPException(
            status_code=413,
            detail="File exceeds the 10 MB upload limit.",
        )

    configure_cloudinary()

    try:
        result = cloudinary.uploader.upload(
            contents,
            folder=folder,
            resource_type="auto",
            original_filename=file.filename,
            use_filename=True,
            unique_filename=True,
        )
    except Exception as exc:
        raise HTTPException(
            status_code=502,
            detail="Cloudinary upload failed.",
        ) from exc

    record = {
        "listing_id": listing_id,
        "original_filename": file.filename,
        "content_type": file.content_type,
        "public_id": result.get("public_id"),
        "url": result.get("secure_url") or result.get("url"),
        "resource_type": result.get("resource_type"),
        "format": result.get("format"),
        "bytes": result.get("bytes"),
        "width": result.get("width"),
        "height": result.get("height"),
        "folder": result.get("folder"),
    }

    try:
        stored = save_upload_record(record)
    except Exception as exc:
        raise HTTPException(
            status_code=502,
            detail="File uploaded, but saving metadata to MongoDB failed.",
        ) from exc

    return {
        "message": "File uploaded successfully",
        "upload": stored,
    }


@router.get("")
def list_media():
    try:
        items = list_upload_records()
    except Exception as exc:
        raise HTTPException(
            status_code=502,
            detail="Failed to load uploads from MongoDB.",
        ) from exc

    return {
        "count": len(items),
        "uploads": items,
    }
