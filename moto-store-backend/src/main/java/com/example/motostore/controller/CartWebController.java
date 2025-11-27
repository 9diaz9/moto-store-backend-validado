package com.example.motostore.controller;

import com.example.motostore.model.Cart;
import com.example.motostore.model.Customer;
import com.example.motostore.model.PaymentMethod;
import com.example.motostore.model.Sale;
import com.example.motostore.repository.CustomerRepository;
import com.example.motostore.service.CartService;
import com.example.motostore.service.SaleService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/cart")
public class CartWebController {

    private final CartService cartService;
    private final CustomerRepository customerRepository;
    private final SaleService saleService;

    public CartWebController(CartService cartService,
                             CustomerRepository customerRepository,
                             SaleService saleService) {
        this.cartService = cartService;
        this.customerRepository = customerRepository;
        this.saleService = saleService;
    }

    // ==========================================================
    // VER CARRITO DEL USUARIO LOGUEADO
    // ==========================================================
    @GetMapping
    public String getCart(Model model) {

        Long customerId = getCurrentCustomerId();
        Cart cart = cartService.getOrCreateCart(customerId);

        model.addAttribute("items", cart.getItems());

        long total = cart.getItems().stream()
                .mapToLong(i -> i.getMoto().getPrice().longValue() * i.getQuantity())
                .sum();
        model.addAttribute("total", total);

        model.addAttribute("paymentMethods", PaymentMethod.values());

        return "cart"; // plantilla cart.html
    }

    // ==========================================================
    // AGREGAR ITEM
    // ==========================================================
    @PostMapping("/add")
    public String addItem(@RequestParam Long motoId,
                          @RequestParam(defaultValue = "1") int quantity,
                          RedirectAttributes redirectAttributes) {

        Long customerId = getCurrentCustomerId();
        try {
            cartService.addItem(customerId, motoId, quantity);
            return "redirect:/cart";
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
            return "redirect:/catalog";
        }
    }

    // ==========================================================
    // REMOVER ITEM
    // ==========================================================
    @PostMapping("/remove")
    public String removeItem(@RequestParam Long motoId) {

        Long customerId = getCurrentCustomerId();
        cartService.removeItem(customerId, motoId);
        return "redirect:/cart";
    }

    // ==========================================================
    // VACIAR CARRITO (BOTÓN "Vaciar carrito")
    // ==========================================================
    @PostMapping("/clear")
    public String clearCart() {

        Long customerId = getCurrentCustomerId();
        cartService.clearCart(customerId);
        return "redirect:/cart";
    }

    // ==========================================================
    // CHECKOUT (CONFIRMAR COMPRA)
    // ==========================================================
    @PostMapping("/checkout")
    public String checkout(@RequestParam("paymentMethod") String paymentMethod,
                           RedirectAttributes redirectAttributes) {

        Long customerId = getCurrentCustomerId();
        Cart cart = cartService.getOrCreateCart(customerId);

        if (cart.getItems().isEmpty()) {
            redirectAttributes.addFlashAttribute(
                    "error",
                    "Tu carrito está vacío, agrega al menos una moto antes de confirmar la compra."
            );
            return "redirect:/cart";
        }

        // Llamamos a la lógica REAL de ventas
        // El tercer parámetro es el tipo de entrega, por ahora "LOCAL"
        Sale sale = saleService.checkout(customerId, paymentMethod, "LOCAL");

        // En SaleService.checkout normalmente se:
        //  - crea la entidad Sale
        //  - descuenta stock
        //  - genera factura PDF (si lo tienes implementado)
        //  - limpia el carrito
        // Así que NO volvemos a llamar a clearCart aquí.

        redirectAttributes.addFlashAttribute(
                "success",
                "Compra realizada exitosamente. Factura #" + sale.getId() + " generada."
        );

        // Volvemos al carrito: aparecerá vacío PERO con el mensaje de éxito
        return "redirect:/cart";
    }

    // ==========================================================
    // OBTENER ID DEL CLIENTE LOGUEADO
    // ==========================================================
    private Long getCurrentCustomerId() {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName(); // correo del usuario logueado

        Customer customer = customerRepository
                .findByUserEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("Cliente no encontrado para el usuario: " + email));

        return customer.getId();
    }
}
