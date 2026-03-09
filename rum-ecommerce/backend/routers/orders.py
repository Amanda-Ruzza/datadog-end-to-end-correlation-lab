"""
Orders/cart routes: add to cart (optional), checkout (create purchase).
"""
from fastapi import APIRouter, HTTPException

from backend.schemas import CheckoutRequest, PurchaseResponse
from backend import database

router = APIRouter(prefix="/orders", tags=["orders"])


@router.post("/checkout", response_model=PurchaseResponse)
def checkout(body: CheckoutRequest):
    """
    Create a purchase from cart items and customer info.
    If customer_id is provided (signed-in user), we link the purchase to that customer.
    """
    if not body.items:
        raise HTTPException(status_code=400, detail="Cart is empty")

    email = body.customer.email
    customer_id = body.customer_id

    # If no customer_id, optionally find or create customer by email (for guest checkout we still store customer info)
    if not customer_id:
        existing = database.find_customer_by_email(email)
        if existing:
            customer_id = existing.get("id") or str(existing.get("_id"))

    # Build line items and total
    items_for_db = []
    total = 0.0
    for it in body.items:
        line_total = it.price * it.quantity
        total += line_total
        items_for_db.append({
            "sku": it.sku,
            "title": it.title,
            "quantity": it.quantity,
            "price": it.price,
        })

    order_id = database.create_purchase(
        customer_id=customer_id,
        email=email,
        items=items_for_db,
        total=total,
    )
    return PurchaseResponse(success=True, order_id=order_id)
