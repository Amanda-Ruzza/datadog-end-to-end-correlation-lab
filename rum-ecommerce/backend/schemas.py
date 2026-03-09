"""
Pydantic models for request/response and internal data.
"""
from datetime import datetime
from typing import Optional

from pydantic import BaseModel, EmailStr, Field


# ---- Auth ----
class SignupRequest(BaseModel):
    first_name: str = Field(..., min_length=1)
    last_name: str = Field(..., min_length=1)
    email: EmailStr


class SigninRequest(BaseModel):
    email: EmailStr


class AuthResponse(BaseModel):
    success: bool
    customer_id: Optional[str] = None
    message: Optional[str] = None


# ---- Cart / Orders ----
class CartItem(BaseModel):
    sku: str
    title: str
    quantity: int = Field(..., ge=1)
    price: float = Field(..., ge=0)


class AddToCartRequest(BaseModel):
    sku: str
    quantity: int = Field(..., ge=1)


class CheckoutCustomer(BaseModel):
    """Customer info at checkout (guest or registered)."""
    first_name: str = Field(..., min_length=1)
    last_name: str = Field(..., min_length=1)
    email: EmailStr


class CheckoutRequest(BaseModel):
    items: list[CartItem]
    customer: CheckoutCustomer
    customer_id: Optional[str] = None  # If already signed in


class PurchaseResponse(BaseModel):
    success: bool
    order_id: Optional[str] = None
    message: Optional[str] = None


# ---- Internal / DB representation ----
class CustomerInDB(BaseModel):
    id: Optional[str] = None
    first_name: str
    last_name: str
    email: str
    created_at: Optional[datetime] = None

    class Config:
        from_attributes = True


class PurchaseInDB(BaseModel):
    id: Optional[str] = None
    customer_id: Optional[str] = None
    email: str
    items: list[dict]  # [{ sku, title, quantity, price }]
    total: float
    created_at: Optional[datetime] = None

    class Config:
        from_attributes = True
