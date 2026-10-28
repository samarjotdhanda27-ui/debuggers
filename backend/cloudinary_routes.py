import os
from typing import Optional
from fastapi import APIRouter, HTTPException, UploadFile, File
from pydantic import BaseModel
from dotenv import load_dotenv

load_dotenv()

router = APIRouter(prefix="/upload", tags=["Cloudinary Media Upload"])


class ImageUploadPayload(BaseModel):
    file_payload: str
    folder: Optional[str] = "reloop_materials"


def configure_cloudinary():
    try:
        import cloudinary
        cloudinary.config(
            cloud_name=os.getenv("CLOUDINARY_CLOUD_NAME", "ru3ejcrx"),
            api_key=os.getenv("CLOUDINARY_API_KEY", "795384193437113"),
            api_secret=os.getenv("CLOUDINARY_API_SECRET", "3UKWF01hPut_7UVUgGLUyc8Yqx0"),
            secure=True
        )
        return True
    except Exception as e:
        print(f"[Warning] Cloudinary config error: {e}")
        return False


@router.post("/image")
def upload_image_payload(payload: ImageUploadPayload):
    if not configure_cloudinary():
        raise HTTPException(status_code=500, detail="Cloudinary SDK not available")

    try:
        import cloudinary.uploader
        res = cloudinary.uploader.upload(
            payload.file_payload,
            folder=payload.folder or "reloop_materials",
            resource_type="image",
            overwrite=True
        )
        return {
            "success": True,
            "url": res.get("secure_url", res.get("url")),
            "public_id": res.get("public_id")
        }
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Cloudinary upload failed: {str(e)}")


@router.post("/file")
def upload_multipart_file(file: UploadFile = File(...)):
    if not configure_cloudinary():
        raise HTTPException(status_code=500, detail="Cloudinary SDK not available")

    try:
        import cloudinary.uploader
        content = file.file.read()
        res = cloudinary.uploader.upload(
            content,
            folder="reloop_materials",
            resource_type="image"
        )
        return {
            "success": True,
            "url": res.get("secure_url", res.get("url")),
            "public_id": res.get("public_id")
        }
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"File upload failed: {str(e)}")
