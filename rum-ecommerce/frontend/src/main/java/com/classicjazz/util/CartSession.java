package com.classicjazz.util;

import com.classicjazz.model.Cart;
import jakarta.servlet.http.HttpSession;

/**
 * Ensures the cart is stored under a fixed session attribute so it survives
 * redirects and matches between POST /cart/add and GET /cart (avoids relying
 * solely on the session-scoped proxy, which can fail when the cookie isn't sent).
 */
public final class CartSession {

    public static final String SESSION_ATTR = "classicJazzCart";

    private CartSession() {}

    public static Cart getOrCreate(HttpSession session) {
        Cart cart = (Cart) session.getAttribute(SESSION_ATTR);
        if (cart == null) {
            cart = new Cart();
            session.setAttribute(SESSION_ATTR, cart);
        }
        return cart;
    }
}
