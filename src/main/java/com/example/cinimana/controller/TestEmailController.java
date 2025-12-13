package com.example.cinimana.controller;

import com.example.cinimana.service.EmailService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;

@RestController
@RequestMapping("/test-email")
public class TestEmailController {

    private final EmailService emailService;

    public TestEmailController(EmailService emailService) {
        this.emailService = emailService;
    }

    @GetMapping
    public String send() {
        emailService.sendInitialPasswordEmail(
                "safaa.analisse1@gmail.com",
                "Test123!",
                "test-login"
        );
        return "Email envoyé (si tout est bien configuré) !";
    }
}
