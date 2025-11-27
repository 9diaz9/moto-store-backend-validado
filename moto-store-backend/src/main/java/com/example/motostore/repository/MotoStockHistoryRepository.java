package com.example.motostore.repository;

import com.example.motostore.model.MotoStockHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MotoStockHistoryRepository extends JpaRepository<MotoStockHistory, Long> {

}
