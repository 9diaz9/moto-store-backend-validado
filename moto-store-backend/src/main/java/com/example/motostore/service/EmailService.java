package com.example.motostore.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.internet.MimeMessage;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    // Usamos el usuario configurado en spring.mail.username como remitente
    @Value("${spring.mail.username:no-reply@motostore-suzuki.com}")
    private String fromAddress;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    /**
     * Envía un correo de verificación con HTML.
     * Si algo falla al enviar HTML, hace fallback a texto plano.
     */
    public void sendVerificationEmail(String to, String code) {
        String subject = "Verifica tu cuenta en MotoStore Suzuki";

        // Link de verificación
        String verificationLink = "http://localhost:8080/verify?code=" +
                URLEncoder.encode(code, StandardCharsets.UTF_8);

        // Cuerpo en HTML
        String html = """
                <h2>MotoStore Suzuki</h2>
                <p>Gracias por registrarte. Para activar tu cuenta, haz clic en el siguiente enlace:</p>
                <p><a href="%s">Verificar cuenta</a></p>
                <p>O usa este código en la página de verificación:</p>
                <h3>%s</h3>
                <p>Si no fuiste tú, puedes ignorar este correo.</p>
                """.formatted(verificationLink, code);

        try {
            // ---------- Correo HTML ----------
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper =
                    new MimeMessageHelper(message, MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED, "UTF-8");

            helper.setFrom(fromAddress);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(html, true); // true = HTML

            mailSender.send(message);

        } catch (Exception e) {
            // Si falla el envío HTML, hacemos fallback a texto plano
            sendPlainTextVerificationEmail(to, subject, verificationLink, code);
        }
    }

    /**
     * Envía un correo de verificación en texto plano.
     */
    private void sendPlainTextVerificationEmail(String to, String subject,
                                                String verificationLink, String code) {

        String text = """
                MotoStore Suzuki

                Gracias por registrarte. Para activar tu cuenta, visita el siguiente enlace:
                %s

                O usa este código en la página de verificación:
                %s

                Si no fuiste tú, puedes ignorar este correo.
                """.formatted(verificationLink, code);

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromAddress);
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);

        mailSender.send(message);
    }

    /**
     * Envía un correo de prueba simple.
     */
    public void sendTestEmail() {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromAddress);
        message.setTo("bndfnmn@gmail.com");  // cámbialo si quieres
        message.setSubject("Correo de prueba desde MotoStore Suzuki");
        message.setText("¡Este es un correo de prueba enviado desde la aplicación MotoStore Suzuki!");

        mailSender.send(message);
    }

    /**
 * Envía al cliente una factura en PDF adjunta.
 */
        public void sendInvoiceEmail(String to, byte[] pdfBytes, String fileName) {
            try {
        MimeMessage message = mailSender.createMimeMessage();

        MimeMessageHelper helper = new MimeMessageHelper(
                message,
                MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED,
                "UTF-8"
        );

        helper.setFrom(fromAddress);
        helper.setTo(to);
        helper.setSubject("Factura de tu compra en MotoStore Suzuki");
        helper.setText("""
                <h2>Gracias por tu compra</h2>
                <p>Adjuntamos la factura electrónica correspondiente a tu pedido.</p>
                <p>Si no reconoces esta compra, por favor contáctanos.</p>
                """, true);

        // Adjuntar PDF
        helper.addAttachment(fileName, () -> new java.io.ByteArrayInputStream(pdfBytes));

        mailSender.send(message);

    } catch (Exception e) {
        throw new RuntimeException("Error enviando la factura por correo", e);
    }
} 
}

