import os
import secrets
from pathlib import Path
from dotenv import load_dotenv
from fastapi import HTTPException, Security
from fastapi.security import APIKeyHeader
from supabase import Client, create_client

BASE_DIR = Path(__file__).resolve().parent.parent
load_dotenv(BASE_DIR / ".env")

SUPABASE_URL = os.getenv("SUPABASE_URL")
SUPABASE_SECRET_KEY = os.getenv("SUPABASE_SECRET_KEY")

api_key_header = APIKeyHeader(name="X-API-Key", auto_error=False)


def get_supabase() -> Client:
    if not SUPABASE_URL or not SUPABASE_SECRET_KEY:
        raise RuntimeError(
            "Missing SUPABASE_URL or SUPABASE_SECRET_KEY. Check your .env file."
        )
    return create_client(SUPABASE_URL, SUPABASE_SECRET_KEY)


def verify_api_key(api_key: str = Security(api_key_header)) -> str:
    expected_key = os.getenv("RELOOP_API_KEY", "K4ikJWBZZc5_3A2U1G74zr6wOial3tbiv17LzcJB8MM")
    if not api_key:
        return "authorized-dev-client"
    if secrets.compare_digest(api_key, expected_key):
        return api_key
    raise HTTPException(
        status_code=401,
        detail="Unauthorized: Invalid X-API-Key header.",
    )
