"""
MongoDB connection and collection access.
Collections: customers, purchases.
"""
import os
from datetime import datetime

from pymongo import MongoClient
from pymongo.database import Database
from pymongo.collection import Collection

# Default for Docker Compose; override with MONGODB_URI for local (e.g. localhost:27017)
MONGODB_URI = os.getenv("MONGODB_URI", "mongodb://mongodb:27017")
DB_NAME = "classic_jazz"

_client: MongoClient | None = None


def get_client() -> MongoClient:
    global _client
    if _client is None:
        _client = MongoClient(MONGODB_URI)
    return _client


def get_db() -> Database:
    return get_client()[DB_NAME]


def get_customers_collection() -> Collection:
    return get_db()["customers"]


def get_purchases_collection() -> Collection:
    return get_db()["purchases"]


def close_client() -> None:
    global _client
    if _client is not None:
        _client.close()
        _client = None


def _doc_with_id(doc: dict) -> dict:
    if doc and "_id" in doc:
        doc["id"] = str(doc["_id"])
        doc["_id"] = str(doc["_id"])
    return doc


def create_customer(first_name: str, last_name: str, email: str) -> str:
    coll = get_customers_collection()
    now = datetime.utcnow()
    doc = {
        "first_name": first_name,
        "last_name": last_name,
        "email": email.lower(),
        "created_at": now,
    }
    result = coll.insert_one(doc)
    return str(result.inserted_id)


def find_customer_by_email(email: str) -> dict | None:
    coll = get_customers_collection()
    doc = coll.find_one({"email": email.lower()})
    if doc:
        _doc_with_id(doc)
    return doc


def find_customer_by_id(customer_id: str) -> dict | None:
    from bson import ObjectId
    coll = get_customers_collection()
    try:
        doc = coll.find_one({"_id": ObjectId(customer_id)})
        if doc:
            _doc_with_id(doc)
        return doc
    except Exception:
        return None


def create_purchase(customer_id: str | None, email: str, items: list[dict], total: float) -> str:
    coll = get_purchases_collection()
    now = datetime.utcnow()
    doc = {
        "customer_id": customer_id,
        "email": email.lower(),
        "items": items,
        "total": round(total, 2),
        "created_at": now,
    }
    result = coll.insert_one(doc)
    return str(result.inserted_id)
