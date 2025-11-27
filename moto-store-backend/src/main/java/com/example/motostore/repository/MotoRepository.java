package com.example.motostore.repository;

import com.example.motostore.model.Moto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface MotoRepository extends JpaRepository<Moto, Long> {

    List<Moto> findByActiveTrue();

    List<Moto> findByBrandAndModel(String brand, String model);

    // Inventario por referencia (marca + modelo)
    @Query("""
           SELECT m.brand AS brand,
                  m.model AS model,
                  SUM(m.stock) AS totalStock
           FROM Moto m
           WHERE m.active = true
           GROUP BY m.brand, m.model
           """)
    List<InventoryByModelView> getInventoryByModel();

    // Proyección para el dashboard
    interface InventoryByModelView {
        String getBrand();
        String getModel();
        Long getTotalStock();
    }
}
