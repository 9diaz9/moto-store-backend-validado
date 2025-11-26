package com.example.motostore.controller;

import com.example.motostore.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestMailController {

    @Autowired
    private EmailService emailService;

    @GetMapping("/send-test-email")
    public String sendEmail() {
        emailService.sendTestEmail();
        return "Correo enviado!";
    }
}
