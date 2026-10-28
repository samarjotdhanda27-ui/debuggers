import os
from dotenv import load_dotenv

load_dotenv()

_client = None


def get_mongodb():
    global _client
    from pymongo import MongoClient

    uri = os.getenv("MONGODB_URI")
    if not uri:
        raise RuntimeError("MONGODB_URI is not configured in .env")

    if _client is None:
        _client = MongoClient(
            uri,
            serverSelectionTimeoutMS=5000,
        )

    db_name = os.getenv("DATABASE_NAME", "reloop")
    return _client[db_name]


def get_collection(collection_name: str):
    db = get_mongodb()
    return db[collection_name]


def check_mongodb_connection():
    try:
        from pymongo.errors import PyMongoError
        db = get_mongodb()
        db.command("ping")
        return True
    except Exception:
        return False
