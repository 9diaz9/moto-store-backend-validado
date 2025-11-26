package com.example.motostore.repository;

import com.example.motostore.model.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CustomerRepository extends JpaRepository<Customer, Long> {

    // Busca el cliente por el email del usuario asociado
    Optional<Customer> findByUserEmail(String email);
}
