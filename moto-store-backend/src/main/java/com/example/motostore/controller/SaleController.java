package com.example.motostore.controller;

import com.example.motostore.model.Sale;
import com.example.motostore.service.SaleService;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.file.Files;
import java.nio.file.Path;

@RestController
@RequestMapping("/api/sales")
@CrossOrigin
public class SaleController {

    private final SaleService saleService;

    public SaleController(SaleService saleService) {
        this.saleService = saleService;
    }

    // Checkout: crea la venta a partir del carrito del cliente
    @PostMapping("/checkout/{customerId}")
    public Sale checkout(@PathVariable Long customerId,
                         @RequestParam String paymentMethod,
                         @RequestParam String deliveryType) {
        return saleService.checkout(customerId, paymentMethod, deliveryType);
    }

    // Descargar factura en PDF
    @GetMapping("/{id}/invoice")
    public ResponseEntity<ByteArrayResource> downloadInvoice(@PathVariable Long id) throws Exception {
        Sale sale = saleService.findById(id);

        Path pdfPath = Path.of(sale.getInvoicePdfPath());
        byte[] data = Files.readAllBytes(pdfPath);
        ByteArrayResource resource = new ByteArrayResource(data);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + pdfPath.getFileName())
                .contentType(MediaType.APPLICATION_PDF)
                .contentLength(data.length)
                .body(resource);
    }
}
