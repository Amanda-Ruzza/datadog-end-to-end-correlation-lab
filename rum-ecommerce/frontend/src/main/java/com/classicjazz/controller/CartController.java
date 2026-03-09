package com.classicjazz.controller;

import com.classicjazz.client.BackendClient;
import com.classicjazz.model.Cart;
import com.classicjazz.service.UserContextService;
import com.classicjazz.util.CartSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttribute;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpSession;
import java.util.List;
import java.util.stream.Collectors;

@Controller
public class CartController {

    private final BackendClient backendClient;
    private final UserContextService userContextService;

    public CartController(BackendClient backendClient, UserContextService userContextService) {
        this.backendClient = backendClient;
        this.userContextService = userContextService;
    }

    @GetMapping("/cart")
    public String cart(HttpSession session, Model model,
                      @SessionAttribute(name = "customerId", required = false) String customerId,
                      @SessionAttribute(name = "customerFirstName", required = false) String customerFirstName,
                      @SessionAttribute(name = "customerLastName", required = false) String customerLastName,
                      @SessionAttribute(name = "customerEmail", required = false) String customerEmail) {
        Cart cart = CartSession.getOrCreate(session);
        model.addAttribute("cart", cart);
        model.addAttribute("customerId", customerId);
        // Pre-fill checkout form: use session (from signup) or fetch customer (from signin)
        if (customerFirstName != null && customerLastName != null && customerEmail != null) {
            model.addAttribute("checkoutFirstName", customerFirstName);
            model.addAttribute("checkoutLastName", customerLastName);
            model.addAttribute("checkoutEmail", customerEmail);
        } else if (customerId != null && !customerId.isBlank()) {
            try {
                var c = backendClient.getCustomer(customerId);
                model.addAttribute("checkoutFirstName", c.first_name());
                model.addAttribute("checkoutLastName", c.last_name());
                model.addAttribute("checkoutEmail", c.email());
            } catch (BackendClient.BackendException ignored) {
                // e.g. 404 — leave form empty
            }
        }
        return "cart";
    }

    @PostMapping("/cart/checkout")
    public String checkout(@RequestParam String firstName,
                           @RequestParam String lastName,
                           @RequestParam String email,
                           @SessionAttribute(name = "customerId", required = false) String customerId,
                           HttpSession session,
                           RedirectAttributes redirectAttributes) {
        Cart cart = CartSession.getOrCreate(session);
        if (cart.getItems().isEmpty()) {
            redirectAttributes.addFlashAttribute("checkoutError", "Your cart is empty.");
            return "redirect:/cart";
        }
        List<java.util.Map<String, Object>> itemsAsMaps = cart.getItems().stream()
                .map(item -> java.util.Map.<String, Object>of(
                        "sku", item.sku(),
                        "title", item.title(),
                        "quantity", item.quantity(),
                        "price", item.price()
                ))
                .collect(Collectors.toList());
        try {
            var resp = backendClient.checkout(firstName, lastName, email, customerId, itemsAsMaps);
            if (resp.success()) {
                cart.clear();
                // Only update user_context.json for signed-in users; guest checkouts must not overwrite it (so load gen's fake user stays).
                if (customerId != null && !customerId.isBlank()) {
                    userContextService.writeUserContext(customerId, firstName + " " + lastName, email);
                }
                redirectAttributes.addFlashAttribute("orderId", resp.order_id());
                redirectAttributes.addFlashAttribute("checkoutSuccess", true);
                return "redirect:/cart";
            }
        } catch (BackendClient.BackendException e) {
            redirectAttributes.addFlashAttribute("checkoutError", friendlyCheckoutError(e.getBody()));
            return "redirect:/cart";
        }
        return "redirect:/cart";
    }

    /** Extract a short message from FastAPI error JSON (detail can be string or array). */
    private static String friendlyCheckoutError(String body) {
        if (body == null || body.isBlank()) return "Checkout failed. Please try again.";
        // FastAPI 422/400 often returns {"detail": "..."} or {"detail": [{"msg": "..."}]}
        if (body.contains("\"detail\"")) {
            try {
                int start = body.indexOf("\"detail\"");
                int valueStart = body.indexOf(":", start) + 1;
                if (body.charAt(valueStart) == '"') {
                    int end = body.indexOf("\"", valueStart + 1);
                    return body.substring(valueStart + 1, end);
                }
                if (body.charAt(valueStart) == '[') {
                    int msgStart = body.indexOf("\"msg\"", valueStart);
                    if (msgStart > 0) {
                        int msgVal = body.indexOf(":", msgStart) + 1;
                        int q = body.indexOf("\"", msgVal + 1);
                        int q2 = body.indexOf("\"", q + 1);
                        return body.substring(q + 1, q2);
                    }
                }
            } catch (Exception ignored) { }
        }
        return body.length() > 200 ? body.substring(0, 200) + "…" : body;
    }
}
