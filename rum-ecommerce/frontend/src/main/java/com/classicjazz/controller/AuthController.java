package com.classicjazz.controller;

import com.classicjazz.client.BackendClient;
import com.classicjazz.service.UserContextService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttribute;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpSession;

@Controller
public class AuthController {

    private final BackendClient backendClient;
    private final UserContextService userContextService;

    public AuthController(BackendClient backendClient, UserContextService userContextService) {
        this.backendClient = backendClient;
        this.userContextService = userContextService;
    }

    @GetMapping("/auth")
    public String authPage() {
        return "auth";
    }

    @PostMapping("/auth/signup")
    public String signup(@RequestParam String firstName,
                         @RequestParam String lastName,
                         @RequestParam String email,
                         HttpSession session,
                         RedirectAttributes redirectAttributes) {
        try {
            var resp = backendClient.signup(firstName, lastName, email);
            if (resp.success() && resp.customer_id() != null) {
                session.setAttribute("customerId", resp.customer_id());
                session.setAttribute("customerFirstName", firstName);
                session.setAttribute("customerLastName", lastName);
                session.setAttribute("customerEmail", email);
                userContextService.writeUserContext(resp.customer_id(), firstName + " " + lastName, email);
                redirectAttributes.addFlashAttribute("authMessage", "Welcome, " + firstName + "! You are signed up.");
                return "redirect:/";
            }
        } catch (BackendClient.BackendException e) {
            redirectAttributes.addFlashAttribute("authError", e.getBody());
            return "redirect:/auth";
        }
        return "redirect:/auth";
    }

    @PostMapping("/auth/signin")
    public String signin(@RequestParam String email,
                         HttpSession session,
                         RedirectAttributes redirectAttributes) {
        try {
            var resp = backendClient.signin(email);
            if (resp.success() && resp.customer_id() != null) {
                session.setAttribute("customerId", resp.customer_id());
                try {
                    var c = backendClient.getCustomer(resp.customer_id());
                    String name = (c.first_name() != null ? c.first_name() : "").trim() + " "
                            + (c.last_name() != null ? c.last_name() : "").trim();
                    session.setAttribute("customerFirstName", c.first_name());
                    session.setAttribute("customerLastName", c.last_name());
                    session.setAttribute("customerEmail", c.email());
                    userContextService.writeUserContext(resp.customer_id(), name.trim(), c.email());
                } catch (BackendClient.BackendException e) {
                    userContextService.writeUserContext(resp.customer_id(), null, email);
                }
                redirectAttributes.addFlashAttribute("authMessage", "Welcome back!");
                return "redirect:/";
            }
        } catch (BackendClient.BackendException e) {
            redirectAttributes.addFlashAttribute("authError", e.getBody());
            return "redirect:/auth";
        }
        return "redirect:/auth";
    }

    @PostMapping("/auth/signout")
    public String signout(HttpSession session, RedirectAttributes redirectAttributes) {
        session.removeAttribute("customerId");
        session.removeAttribute("customerFirstName");
        session.removeAttribute("customerLastName");
        session.removeAttribute("customerEmail");
        redirectAttributes.addFlashAttribute("authMessage", "You have signed out.");
        return "redirect:/";
    }
}
