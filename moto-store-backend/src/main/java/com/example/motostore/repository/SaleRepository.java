package com.example.motostore.repository;

import com.example.motostore.model.Sale;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface SaleRepository extends JpaRepository<Sale, Long> {

    List<Sale> findByCustomerId(Long customerId);

    // NUEVO: historial de compras del cliente, ordenado por fecha
    List<Sale> findByCustomerIdOrderBySaleDateDesc(Long customerId);

    // Ventas por mes (año, mes, total, promedio, cantidad)
    @Query("""
           SELECT YEAR(s.saleDate) AS year,
                  MONTH(s.saleDate) AS month,
                  SUM(s.total) AS totalAmount,
                  AVG(s.total) AS avgAmount,
                  COUNT(s) AS saleCount
           FROM Sale s
           GROUP BY YEAR(s.saleDate), MONTH(s.saleDate)
           ORDER BY year DESC, month DESC
           """)
    List<MonthlySalesView> getMonthlySalesSummary();

    // Medios de pago en un rango de fechas
    @Query("""
           SELECT s.paymentMethod AS paymentMethod,
                  COUNT(s) AS count,
                  SUM(s.total) AS total
           FROM Sale s
           WHERE s.saleDate >= :start AND s.saleDate < :end
           GROUP BY s.paymentMethod
           """)
    List<PaymentMethodSummaryView> getPaymentMethodSummary(LocalDateTime start,
                                                           LocalDateTime end);

    // Proyección: ventas por mes
    interface MonthlySalesView {
        Integer getYear();
        Integer getMonth();
        BigDecimal getTotalAmount();
        BigDecimal getAvgAmount();
        Long getSaleCount();
    }

    // Proyección: medios de pago
    interface PaymentMethodSummaryView {
        String getPaymentMethod();
        Long getCount();
        BigDecimal getTotal();
    }
}
