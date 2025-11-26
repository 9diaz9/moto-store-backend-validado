package com.example.motostore.service;

import com.example.motostore.model.*;
import com.example.motostore.repository.CartRepository;
import com.example.motostore.repository.CustomerRepository;
import com.example.motostore.repository.MotoRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class CartService {

    private final CartRepository cartRepository;
    private final MotoRepository motoRepository;
    private final CustomerRepository customerRepository;

    public CartService(CartRepository cartRepository, MotoRepository motoRepository, CustomerRepository customerRepository) {
        this.cartRepository = cartRepository;
        this.motoRepository = motoRepository;
        this.customerRepository = customerRepository;
    }

    public Cart getOrCreateCart(Long customerId) {
        return cartRepository.findByCustomerId(customerId)
                .orElseGet(() -> {
                    Customer c = customerRepository.findById(customerId)
                            .orElseThrow(() -> new RuntimeException("Cliente no existe"));
                    Cart cart = new Cart();
                    cart.setCustomer(c);
                    return cartRepository.save(cart);
                });
    }

    public Cart addItem(Long customerId, Long motoId, int quantity) {
        Cart cart = getOrCreateCart(customerId);
        Moto moto = motoRepository.findById(motoId)
                .orElseThrow(() -> new RuntimeException("Moto no existe"));

        Optional<CartItem> existing = cart.getItems().stream()
                .filter(ci -> ci.getMoto().getId().equals(motoId))
                .findFirst();

        if (existing.isPresent()) {
            existing.get().setQuantity(existing.get().getQuantity() + quantity);
        } else {
            CartItem item = new CartItem();
            item.setCart(cart);
            item.setMoto(moto);
            item.setQuantity(quantity);
            cart.getItems().add(item);
        }

        return cartRepository.save(cart);
    }

    public Cart removeItem(Long customerId, Long motoId) {
        Cart cart = getOrCreateCart(customerId);
        cart.getItems().removeIf(ci -> ci.getMoto().getId().equals(motoId));
        return cartRepository.save(cart);
    }

    public Cart clearCart(Long customerId) {
        Cart cart = getOrCreateCart(customerId);
        cart.getItems().clear();
        return cartRepository.save(cart);
    }
}
