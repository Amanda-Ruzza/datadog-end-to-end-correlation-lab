"""
Customer lookup for checkout pre-fill (e.g. signed-in user).
"""
from fastapi import APIRouter, HTTPException

from backend import database

router = APIRouter(prefix="/customers", tags=["customers"])


@router.get("/{customer_id}")
def get_customer(customer_id: str):
    """Return customer first_name, last_name, email for pre-filling checkout."""
    customer = database.find_customer_by_id(customer_id)
    if not customer:
        raise HTTPException(status_code=404, detail="Customer not found")
    return {
        "first_name": customer.get("first_name"),
        "last_name": customer.get("last_name"),
        "email": customer.get("email"),
    }
