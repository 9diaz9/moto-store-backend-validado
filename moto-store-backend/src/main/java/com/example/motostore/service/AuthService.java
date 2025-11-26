package com.example.motostore.service;

import com.example.motostore.model.Customer;
import com.example.motostore.model.User;
import com.example.motostore.model.UserRole;
import com.example.motostore.repository.CustomerRepository;
import com.example.motostore.repository.UserRepository;
import com.example.motostore.web.dto.RegisterForm;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Random;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    public AuthService(UserRepository userRepository,
                       CustomerRepository customerRepository,
                       PasswordEncoder passwordEncoder,
                       EmailService emailService) {
        this.userRepository = userRepository;
        this.customerRepository = customerRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
    }

    // =====================================================
    // REGISTRO DE NUEVO CLIENTE + ENVÍO DE CÓDIGO
    // =====================================================
    public void registerNewCustomer(RegisterForm form) {

        // Normalizar correo
        String normalizedEmail = form.getEmail() == null
                ? null
                : form.getEmail().trim().toLowerCase();

        if (normalizedEmail == null || normalizedEmail.isEmpty()) {
            throw new RuntimeException("El correo electrónico es obligatorio.");
        }

        // Validar que no exista ya
        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new RuntimeException("Ya existe un usuario registrado con ese correo.");
        }

        // Crear usuario
        User user = new User();
        user.setEmail(normalizedEmail);
        user.setPassword(passwordEncoder.encode(form.getPassword()));
        user.setRole(UserRole.ROLE_CUSTOMER);
        user.setEnabled(false); // hasta que verifique código

        // Generar código de verificación (PIN de 6 dígitos)
        String code = generateVerificationCode();
        user.setVerificationCode(code);

        // Crear cliente asociado
        Customer customer = new Customer();
        customer.setFullName(form.getFullName());
        customer.setUser(user);
        user.setCustomer(customer);

        // Guardar en BD (por cascada se guarda customer)
        userRepository.save(user);

        // Enviar correo con el código
        emailService.sendVerificationEmail(user.getEmail(), code);
    }

    // =====================================================
    // VERIFICAR CÓDIGO
    // =====================================================
    public boolean verifyCode(String email, String code) {
        if (email == null || code == null) {
            return false;
        }

        String normalizedEmail = email.trim().toLowerCase();

        return userRepository.findByEmail(normalizedEmail)
                .map(user -> {
                    if (user.getVerificationCode() != null
                            && user.getVerificationCode().equals(code)) {
                        user.setEnabled(true);
                        user.setVerificationCode(null); // limpiar código
                        userRepository.save(user);
                        return true;
                    }
                    return false;
                })
                .orElse(false);
    }

    // =====================================================
    // UTILIDAD: GENERAR CÓDIGO DE 6 DÍGITOS
    // =====================================================
    private String generateVerificationCode() {
        Random random = new Random();
        int number = 100000 + random.nextInt(900000); // 6 dígitos
        return String.valueOf(number);
    }
}
