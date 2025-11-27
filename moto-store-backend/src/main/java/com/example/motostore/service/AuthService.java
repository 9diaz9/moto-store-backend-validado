package com.example.motostore.service;

import com.example.motostore.model.Customer;
import com.example.motostore.model.User;
import com.example.motostore.model.UserRole;
import com.example.motostore.repository.CustomerRepository;
import com.example.motostore.repository.UserRepository;
import com.example.motostore.web.dto.RegisterForm;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

 

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;
    public AuthService(UserRepository userRepository,
                       CustomerRepository customerRepository,
                       PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.customerRepository = customerRepository;
        this.passwordEncoder = passwordEncoder;
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
        // Activamos la cuenta inmediatamente (sin verificación por correo)
        user.setEnabled(true);
        user.setVerificationCode(null);

        // Crear cliente asociado
        Customer customer = new Customer();
        customer.setFullName(form.getFullName());
        customer.setUser(user);
        user.setCustomer(customer);

        // Guardar en BD (por cascada se guarda customer)
        userRepository.save(user);

        // No enviamos correo de verificación; el usuario queda activo inmediatamente
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

    // (La verificación por correo fue deshabilitada; no se necesita generar código)
}
