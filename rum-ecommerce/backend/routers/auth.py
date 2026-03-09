"""
Auth routes: signup and signin.
Simple version: signin by email only (no password); signup creates customer and returns customer_id.
"""
from fastapi import APIRouter, HTTPException

from backend.schemas import AuthResponse, SigninRequest, SignupRequest
from backend import database

router = APIRouter(prefix="/auth", tags=["auth"])


@router.post("/signup", response_model=AuthResponse)
def signup(body: SignupRequest):
    """Register a new customer. Returns customer_id on success."""
    existing = database.find_customer_by_email(body.email)
    if existing:
        raise HTTPException(status_code=400, detail="Email already registered")
    customer_id = database.create_customer(
        body.first_name, body.last_name, body.email
    )
    return AuthResponse(success=True, customer_id=customer_id)


@router.post("/signin", response_model=AuthResponse)
def signin(body: SigninRequest):
    """Sign in by email. Returns customer_id if found."""
    customer = database.find_customer_by_email(body.email)
    if not customer:
        raise HTTPException(status_code=404, detail="Customer not found")
    return AuthResponse(
        success=True,
        customer_id=customer.get("id") or str(customer.get("_id")),
    )
