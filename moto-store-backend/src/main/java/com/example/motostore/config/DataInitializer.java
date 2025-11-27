package com.example.motostore.config;

import com.example.motostore.model.User;
import com.example.motostore.model.UserRole;
import com.example.motostore.model.Customer;
import com.example.motostore.model.Cart;
import com.example.motostore.repository.UserRepository;
import com.example.motostore.repository.CustomerRepository;
import com.example.motostore.repository.CartRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataInitializer {

    @Bean
    public CommandLineRunner initUsers(UserRepository userRepository,
                                       CustomerRepository customerRepository,
                                       CartRepository cartRepository,
                                       PasswordEncoder encoder) {
        return args -> {
            // ADMIN
            String email = "admin@motos.com";

            if (!userRepository.existsByEmail(email)) {
                User admin = new User();
                admin.setEmail(email);
                admin.setPassword(encoder.encode("Admin@123"));
                admin.setRole(UserRole.ROLE_SUPER_ADMIN);
                admin.setEnabled(true);
                admin.setVerificationCode(null);
                userRepository.save(admin);
                System.out.println(">>> ADMIN creado: " + email + " / Admin@123");
            }

            // Usuario de prueba 1
            createTestCustomerIfMissing("prueba@motos.com", "Nicolás Pérez", "nicolas@123",
                    userRepository, customerRepository, cartRepository, encoder);

            // Usuario de prueba 2
            createTestCustomerIfMissing("prueba2@motos.com", "María López", "usuario2@123",
                    userRepository, customerRepository, cartRepository, encoder);
        };
    }

    private void createTestCustomerIfMissing(String email, String fullName, String rawPassword,
                                             UserRepository userRepository,
                                             CustomerRepository customerRepository,
                                             CartRepository cartRepository,
                                             PasswordEncoder encoder) {

        if (!userRepository.existsByEmail(email)) {
            User user = new User();
            user.setEmail(email);
            user.setPassword(encoder.encode(rawPassword));
            user.setRole(UserRole.ROLE_CUSTOMER);
            user.setEnabled(true);
            user.setVerificationCode(null);
            User saved = userRepository.save(user);

            Customer customer = new Customer();
            customer.setFullName(fullName);
            customer.setPhone("");
            customer.setAddress("");
            customer.setUser(saved);
            Customer savedCustomer = customerRepository.save(customer);

            Cart cart = new Cart();
            cart.setCustomer(savedCustomer);
            cartRepository.save(cart);

            System.out.println(">>> Usuario de prueba creado: " + email + " / " + rawPassword);
        } else {
            System.out.println(">>> Usuario de prueba ya existe: " + email);

            // Si existe el usuario pero no tiene Customer o Cart, crear vínculos
            userRepository.findByEmail(email).ifPresent(u -> {
                customerRepository.findByUserEmail(email).orElseGet(() -> {
                    Customer customer = new Customer();
                    customer.setFullName(fullName);
                    customer.setPhone("");
                    customer.setAddress("");
                    customer.setUser(u);
                    Customer savedCustomer = customerRepository.save(customer);
                    System.out.println(">>> Customer creado para usuario existente: " + email);
                    // crear carrito si no existe
                    cartRepository.findByCustomerId(savedCustomer.getId()).orElseGet(() -> {
                        Cart cart = new Cart();
                        cart.setCustomer(savedCustomer);
                        cartRepository.save(cart);
                        System.out.println(">>> Cart creado para customer: " + savedCustomer.getId());
                        return cart;
                    });
                    return savedCustomer;
                });
            });
        }
    }
}

