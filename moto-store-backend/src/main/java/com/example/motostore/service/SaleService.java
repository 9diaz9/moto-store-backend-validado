package com.example.motostore.service;

import com.example.motostore.model.*;
import com.example.motostore.repository.CustomerRepository;
import com.example.motostore.repository.SaleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class SaleService {

    private final CartService cartService;
    private final SaleRepository saleRepository;
    private final MotoService motoService;
    private final PdfInvoiceService pdfInvoiceService;
    private final CustomerRepository customerRepository;
    private final EmailService emailService;


    public SaleService(CartService cartService,
                       SaleRepository saleRepository,
                       MotoService motoService,
                       PdfInvoiceService pdfInvoiceService,
                       CustomerRepository customerRepository,
                       EmailService emailService) {
        this.cartService = cartService;
        this.saleRepository = saleRepository;
        this.motoService = motoService;
        this.pdfInvoiceService = pdfInvoiceService;
        this.customerRepository = customerRepository;
        this.emailService = emailService;
    }

    // =====================================================
    // 1. CHECKOUT – VERSIÓN NUEVA (usa PaymentMethod enum)
    //    USADO POR CartWebController
    // =====================================================
    @Transactional
    public Sale createSaleFromCart(Long customerId, PaymentMethod paymentMethod) {

        Cart cart = cartService.getOrCreateCart(customerId);

        if (cart.getItems().isEmpty()) {
            throw new RuntimeException("Carrito vacío");
        }

        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado"));

        Sale sale = new Sale();
        sale.setCustomer(customer);
        sale.setSaleDate(LocalDateTime.now());
        sale.setPaymentMethod(paymentMethod);
        sale.setDeliveryType("LOCAL"); // valor por defecto, si no te pasan otro

        BigDecimal total = BigDecimal.ZERO;
        List<SaleItem> items = new ArrayList<>();

        for (CartItem ci : cart.getItems()) {

            // 1. Disminuir stock
            motoService.decreaseStock(ci.getMoto().getId(), ci.getQuantity());

            // 2. Crear item de la venta
            SaleItem si = new SaleItem();
            si.setSale(sale);
            si.setMoto(ci.getMoto());
            si.setQuantity(ci.getQuantity());
            si.setUnitPrice(ci.getMoto().getPrice());

            BigDecimal subtotal = ci.getMoto().getPrice()
                    .multiply(BigDecimal.valueOf(ci.getQuantity()));
            si.setSubtotal(subtotal);

            total = total.add(subtotal);
            items.add(si);
        }

        sale.setTotal(total);
        sale.setItems(items);

        // Guardar venta
        Sale saved = saleRepository.save(sale);

        // Generar PDF, intentar enviar la factura por correo y guardar la ruta
        try {
            String pdfPath = pdfInvoiceService.generateInvoice(saved);
            if (pdfPath != null) {
                saved.setInvoicePdfPath(pdfPath);
                saved = saleRepository.save(saved);

                // Leer el PDF generado como bytes solo si existe
                java.nio.file.Path path = java.nio.file.Paths.get(pdfPath);
                if (java.nio.file.Files.exists(path)) {
                    byte[] pdfBytes = java.nio.file.Files.readAllBytes(path);
                    try {
                        String email = customer.getUser().getEmail();  // correo del cliente
                        String fileName = "factura_" + saved.getId() + ".pdf";
                        emailService.sendInvoiceEmail(email, pdfBytes, fileName);
                    } catch (Exception e) {
                        // No lanzamos excepción que provoque rollback; solo registramos el problema
                        System.err.println("Error enviando la factura por correo: " + e.getMessage());
                        e.printStackTrace();
                    }
                } else {
                    System.err.println("Invoice PDF no encontrado en ruta: " + pdfPath);
                }
            } else {
                System.err.println("pdfInvoiceService devolvió ruta nula para la factura de la venta " + saved.getId());
            }
        } catch (Exception e) {
            // Capturamos cualquier error de generación/lectura/envío y no propagamos
            System.err.println("Error generando/enviando factura: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // Limpiar carrito siempre; si la generación/envío falla no debe revertir la compra
            try {
                cartService.clearCart(customerId);
            } catch (Exception e) {
                System.err.println("Error limpiando carrito tras la compra: " + e.getMessage());
                e.printStackTrace();
            }
        }

        return saved;
    }

    // =====================================================
    // 1b. CHECKOUT – VERSIÓN ANTIGUA (String paymentMethod, String deliveryType)
    //     USADO POR SaleController (lo que te marca error en la captura)
    // =====================================================
    @Transactional
    public Sale checkout(Long customerId, String paymentMethod, String deliveryType) {

        // Convertimos el String al enum PaymentMethod
        PaymentMethod pm;
        try {
            pm = PaymentMethod.valueOf(paymentMethod);
        } catch (IllegalArgumentException ex) {
            throw new RuntimeException("Método de pago inválido: " + paymentMethod);
        }

        // Reutilizamos la lógica principal
        Sale sale = createSaleFromCart(customerId, pm);

        // Sobrescribimos deliveryType si nos lo pasan desde el controller
        sale.setDeliveryType(deliveryType);

        return saleRepository.save(sale);
    }

    // =====================================================
    // 2. BUSCAR POR ID (USADO POR downloadInvoice)
    // =====================================================
    public Sale findById(Long id) {
        return saleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Venta no encontrada"));
    }

    // =====================================================
    // 3. FUNCIONES PARA EL PANEL ADMIN
    // =====================================================

    // Historial completo de ventas
    public List<Sale> getAllSales() {
        return saleRepository.findAll();
    }

    // Historial de compras de un cliente
public List<Sale> getSalesForCustomer(Long customerId) {
    return saleRepository.findByCustomerIdOrderBySaleDateDesc(customerId);
}


    // Ventas por mes (usa las proyecciones de SaleRepository)
    public List<SaleRepository.MonthlySalesView> getMonthlySalesSummary() {
        return saleRepository.getMonthlySalesSummary();
    }

    // Medios de pago del mes actual
    public List<SaleRepository.PaymentMethodSummaryView> getPaymentMethodSummaryForCurrentMonth() {
        LocalDate firstDay = LocalDate.now().withDayOfMonth(1);
        LocalDate firstDayNextMonth = firstDay.plusMonths(1);
        LocalDateTime start = firstDay.atStartOfDay();
        LocalDateTime end = firstDayNextMonth.atStartOfDay();
        return saleRepository.getPaymentMethodSummary(start, end);
    }
}
