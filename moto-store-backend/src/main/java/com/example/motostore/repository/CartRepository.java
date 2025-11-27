package com.example.motostore.repository;

import com.example.motostore.model.Cart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;

public interface CartRepository extends JpaRepository<Cart, Long> {
    Optional<Cart> findByCustomerId(Long customerId);

    @Query("SELECT COALESCE(SUM(ci.quantity),0) FROM CartItem ci WHERE ci.moto.id = :motoId")
    Long sumQuantityByMotoId(@Param("motoId") Long motoId);
}
