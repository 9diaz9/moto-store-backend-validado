package com.example.motostore.service;

import com.example.motostore.model.*;
import com.example.motostore.repository.CartRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import com.example.motostore.repository.CustomerRepository;
import com.example.motostore.repository.MotoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class CartService {

    private final CartRepository cartRepository;
    private final MotoRepository motoRepository;
    private final CustomerRepository customerRepository;

    @PersistenceContext
    private EntityManager entityManager;


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

        int existingQty = existing.map(ci -> ci.getQuantity() == null ? 0 : ci.getQuantity()).orElse(0);

        Long reservedAllLong = cartRepository.sumQuantityByMotoId(motoId);
        int reservedAll = reservedAllLong == null ? 0 : reservedAllLong.intValue();

        // reservas en otros carros = total reservado - lo que ya está en el carrito actual
        int reservedByOthers = Math.max(0, reservedAll - existingQty);
        int stock = moto.getStock() == null ? 0 : moto.getStock();
        int availableForUser = stock - reservedByOthers;

        if (availableForUser <= 0) {
            throw new RuntimeException("No hay stock disponible para esta moto");
        }

        if (quantity > availableForUser) {
            throw new RuntimeException("Sólo quedan " + availableForUser + " unidades disponibles");
        }

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
        // Remove via collection so Hibernate handles orphanRemoval correctly
        cart.getItems().removeIf(ci -> ci.getMoto().getId().equals(motoId));
        Cart saved = cartRepository.save(cart);
        try { entityManager.flush(); } catch (Exception ignored) {}
        return saved;
    }

    @Transactional
    public Cart clearCart(Long customerId) {
        Cart cart = getOrCreateCart(customerId);
        Long cartId = cart.getId();
        if (cartId != null) {
            // Use orphanRemoval by clearing the collection and saving the cart
            cart.getItems().clear();
            cartRepository.save(cart);
            // flush to execute deletes immediately
            try { entityManager.flush(); } catch (Exception ignored) {}
        }

        // Return a fresh cart from the repository (detached/clean state)
        return cartRepository.findByCustomerId(customerId).orElseGet(() -> {
            Cart newCart = new Cart();
            Customer c = customerRepository.findById(customerId).orElse(null);
            newCart.setCustomer(c);
            return cartRepository.save(newCart);
        });
    }
}
