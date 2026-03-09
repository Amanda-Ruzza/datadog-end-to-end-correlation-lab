package com.classicjazz.controller;

import com.classicjazz.model.Album;
import com.classicjazz.model.Cart;
import com.classicjazz.service.CatalogService;
import com.classicjazz.util.CartSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpSession;

@Controller
public class CatalogController {

    private final CatalogService catalogService;

    public CatalogController(CatalogService catalogService) {
        this.catalogService = catalogService;
    }

    @GetMapping({"/", "/catalog"})
    public String catalog(Model model, HttpSession session) {
        model.addAttribute("albums", catalogService.getAllAlbums());
        Cart cart = CartSession.getOrCreate(session);
        model.addAttribute("cartSize", cart.getItems().size());
        return "catalog";
    }

    @PostMapping("/cart/add")
    public String addToCart(@RequestParam String sku,
                            @RequestParam(defaultValue = "1") int quantity,
                            HttpSession session,
                            RedirectAttributes redirectAttributes) {
        Cart cart = CartSession.getOrCreate(session);
        Album album = catalogService.findBySku(sku);
        if (album != null) {
            cart.add(new Cart.CartItem(album.sku(), album.title(), album.price(), quantity));
            redirectAttributes.addFlashAttribute("added", album.title());
        }
        return "redirect:/cart";
    }
}
