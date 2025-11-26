package com.example.motostore.controller;

import com.example.motostore.model.Customer;
import com.example.motostore.model.Sale;
import com.example.motostore.repository.CustomerRepository;
import com.example.motostore.service.SaleService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/mis-compras")
public class CustomerSalesController {

    private final SaleService saleService;
    private final CustomerRepository customerRepository;

    public CustomerSalesController(SaleService saleService,
                                   CustomerRepository customerRepository) {
        this.saleService = saleService;
        this.customerRepository = customerRepository;
    }

    @GetMapping
    public String mySales(Model model) {
        Long customerId = getCurrentCustomerId();
        List<Sale> sales = saleService.getSalesForCustomer(customerId);

        model.addAttribute("sales", sales);
        return "customer/my-sales"; // plantilla Thymeleaf
    }

    private Long getCurrentCustomerId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName(); // correo del usuario logueado

        Customer customer = customerRepository
                .findByUserEmail(email)
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado para " + email));

        return customer.getId();
    }
}
