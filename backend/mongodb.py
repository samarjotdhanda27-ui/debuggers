import os

from dotenv import load_dotenv
from pymongo import MongoClient
from pymongo.errors import PyMongoError

load_dotenv()

_client = None


def get_mongodb():
    global _client

    uri = os.getenv("MONGODB_URI")

    if not uri:
        raise RuntimeError("MONGODB_URI is not configured")

    if _client is None:
        _client = MongoClient(
            uri,
            serverSelectionTimeoutMS=5000,
        )

    return _client["reloop"]


def check_mongodb_connection():
    try:
        db = get_mongodb()
        db.command("ping")
        return True
    except (PyMongoError, RuntimeError):
        return False