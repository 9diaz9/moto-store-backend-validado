package com.example.motostore.config;

import com.example.motostore.model.User;
import com.example.motostore.model.UserRole;
import com.example.motostore.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataInitializer {

    @Bean
    public CommandLineRunner initUsers(UserRepository userRepository, PasswordEncoder encoder) {
        return args -> {
            String email = "admin@motos.com";

            if (!userRepository.existsByEmail(email)) {
                User admin = new User();
                admin.setEmail(email);
                admin.setPassword(encoder.encode("Admin@123"));  // 👈 contraseña
                admin.setRole(UserRole.ROLE_SUPER_ADMIN);        // 👈 rol
                admin.setEnabled(true);
                admin.setVerificationCode(null);

                userRepository.save(admin);
                System.out.println(">>> ADMIN creado: " + email + " / Admin@123");
            }
        };
    }
}
