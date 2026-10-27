import os
from datetime import datetime, timezone
from urllib.parse import quote_plus
from pathlib import Path

from dotenv import load_dotenv
from pymongo import MongoClient
from pymongo.errors import PyMongoError

BASE_DIR = Path(__file__).resolve().parent.parent
load_dotenv(BASE_DIR / ".env")

_client = None


def _resolve_mongodb_uri() -> str:
    uri = os.getenv("MONGODB_URI", "").strip()
    password = (
        os.getenv("MONGODB_PASSWORD")
        or os.getenv("MONGO_PASSWORD")
        or ""
    ).strip()

    if "<db_password>" in uri and password:
        uri = uri.replace("<db_password>", quote_plus(password))

    return uri


def get_mongodb():
    global _client

    uri = _resolve_mongodb_uri()

    if not uri:
        raise RuntimeError("MONGODB_URI is not configured")

    if _client is None:
        _client = MongoClient(
            uri,
            serverSelectionTimeoutMS=8000,
            connectTimeoutMS=8000,
        )

    db_name = os.getenv("MONGODB_DB", "reloop")
    return _client[db_name]


def check_mongodb_connection():
    try:
        db = get_mongodb()
        db.command("ping")
        return True
    except (PyMongoError, RuntimeError):
        return False


def save_upload_record(record: dict) -> dict:
    db = get_mongodb()
    payload = {
        **record,
        "created_at": datetime.now(timezone.utc).isoformat(),
    }
    result = db.uploads.insert_one(payload)
    payload["id"] = str(result.inserted_id)
    payload.pop("_id", None)
    return payload


def list_upload_records(limit: int = 50) -> list:
    db = get_mongodb()
    docs = (
        db.uploads.find()
        .sort("created_at", -1)
        .limit(limit)
    )
    items = []
    for doc in docs:
        doc["id"] = str(doc.pop("_id"))
        items.append(doc)
    return items
