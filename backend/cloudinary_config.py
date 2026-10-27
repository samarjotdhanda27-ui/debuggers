import os
from pathlib import Path
from urllib.parse import unquote, urlparse

import cloudinary
from dotenv import load_dotenv

BASE_DIR = Path(__file__).resolve().parent.parent
load_dotenv(BASE_DIR / ".env")

_configured = False


def _credentials_from_url(cloudinary_url: str):
    parsed = urlparse(cloudinary_url)
    return {
        "cloud_name": parsed.hostname,
        "api_key": unquote(parsed.username or ""),
        "api_secret": unquote(parsed.password or ""),
    }


def configure_cloudinary():
    global _configured

    if _configured:
        return cloudinary.config()

    cloud_name = os.getenv("CLOUDINARY_CLOUD_NAME", "").strip()
    api_key = os.getenv("CLOUDINARY_API_KEY", "").strip()
    api_secret = os.getenv("CLOUDINARY_API_SECRET", "").strip()
    cloudinary_url = os.getenv("CLOUDINARY_URL", "").strip()

    if (not cloud_name or not api_key or not api_secret) and cloudinary_url:
        from_url = _credentials_from_url(cloudinary_url)
        cloud_name = cloud_name or from_url["cloud_name"]
        api_key = api_key or from_url["api_key"]
        api_secret = api_secret or from_url["api_secret"]

    if not cloud_name or not api_key or not api_secret:
        raise RuntimeError(
            "Cloudinary is not configured. Set CLOUDINARY_URL or "
            "CLOUDINARY_CLOUD_NAME, CLOUDINARY_API_KEY, and CLOUDINARY_API_SECRET."
        )

    cloudinary.config(
        cloud_name=cloud_name,
        api_key=api_key,
        api_secret=api_secret,
        secure=True,
    )
    _configured = True
    return cloudinary.config()


def check_cloudinary_connection():
    try:
        config = configure_cloudinary()
        return bool(config.cloud_name and config.api_key and config.api_secret)
    except RuntimeError:
        return False
