package com.example.motostore.controller;

import com.example.motostore.service.AuthService;
import com.example.motostore.service.EmailService;
import com.example.motostore.web.dto.RegisterForm;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
public class AuthController {

    private final AuthService authService;
    private final EmailService emailService;   // ⬅️ NUEVO

    // ⬇️ Constructor inyectando ambos servicios
    public AuthController(AuthService authService,
                          EmailService emailService) {
        this.authService = authService;
        this.emailService = emailService;
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/register")
    public String showRegisterForm(Model model) {
        model.addAttribute("form", new RegisterForm());
        return "register";
    }

    @PostMapping("/register")
    public String processRegister(@Valid @ModelAttribute("form") RegisterForm form,
                                  BindingResult result,
                                  Model model) {

        if (!form.getPassword().equals(form.getConfirmPassword())) {
            result.rejectValue("confirmPassword", "passwordConfirmed",
                    "Las contraseñas no coinciden");
        }

        if (result.hasErrors()) {
            return "register";
        }

        try {
            authService.registerNewCustomer(form);
            // después de registrarse, lo mandamos a la pantalla de verificar
            model.addAttribute("email", form.getEmail());
            return "verify";
        } catch (RuntimeException ex) {
            model.addAttribute("error", ex.getMessage());
            return "register";
        }
    }

    @PostMapping("/verify")
    public String verify(@RequestParam String email,
                         @RequestParam String code,
                         Model model) {

        boolean ok = authService.verifyCode(email, code);
        if (ok) {
            model.addAttribute("message", "Cuenta verificada. Ahora puedes iniciar sesión.");
            return "login";
        } else {
            model.addAttribute("error", "Código incorrecto o expirado.");
            model.addAttribute("email", email);
            return "verify";
        }
    }

    // ------------- PRUEBA DE CORREO -------------
    @GetMapping("/test-email")
    @ResponseBody
    public String testEmail() {
        // remplaza por tu dirección de inbox de Mailtrap
        emailService.sendVerificationEmail("TU_INBOX@inbox.mailtrap.io", "123456");
        return "OK";
    }
}
