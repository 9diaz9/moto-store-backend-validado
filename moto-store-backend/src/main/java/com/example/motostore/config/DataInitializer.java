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
    //crear usuario de prueba
    User testUser = new User();
    testUser.setEmail("prueba@motos.com");
    testUser.setPassword(encoder.encode("nicolas@123"));
    testUser.setRole(UserRole.ROLE_CUSTOMER);
    testUser.setEnabled(true);
    testUser.setVerificationCode(null);
    userRepository.save(testUser);
    System.out.println(">>> Usuario de prueba creado: prueba@motos.com / nicolas@123");
                userRepository.save(admin);
                System.out.println(">>> ADMIN creado: " + email + " / Admin@123");
            }
        };
    }
}
