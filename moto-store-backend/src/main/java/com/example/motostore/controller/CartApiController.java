package com.example.motostore.controller;

import com.example.motostore.model.Cart;
import com.example.motostore.service.CartService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
@CrossOrigin
public class CartApiController {

    private final CartService cartService;

    public CartApiController(CartService cartService) {
        this.cartService = cartService;
    }

    @GetMapping("/{customerId}")
    public Cart getCart(@PathVariable Long customerId) {
        return cartService.getOrCreateCart(customerId);
    }

    @PostMapping("/{customerId}/add")
    public Cart addItem(@PathVariable Long customerId,
                        @RequestParam Long motoId,
                        @RequestParam int quantity) {
        return cartService.addItem(customerId, motoId, quantity);
    }

    @DeleteMapping("/{customerId}/remove")
    public Cart removeItem(@PathVariable Long customerId,
                           @RequestParam Long motoId) {
        return cartService.removeItem(customerId, motoId);
    }

    @DeleteMapping("/{customerId}/clear")
    public Cart clear(@PathVariable Long customerId) {
        return cartService.clearCart(customerId);
    }
}
