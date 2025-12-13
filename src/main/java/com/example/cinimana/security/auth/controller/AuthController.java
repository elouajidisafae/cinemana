package com.example.cinimana.security.auth.controller;

import com.example.cinimana.security.auth.dto.AuthRequest;
import com.example.cinimana.security.auth.dto.AuthResponse;
import com.example.cinimana.security.auth.dto.RegisterRequest;
import com.example.cinimana.security.auth.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.example.cinimana.dto.request.PasswordResetRequest; // ✅ NOUVEL IMPORT
import com.example.cinimana.service.admin.AdminUserService;
import org.springframework.security.core.Authentication;// ✅ NOUVEL IMPORT

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
public class AuthController {

    private final AuthService authService;
    private final AdminUserService adminUserService;
    // Login pour les clients
    @PostMapping("/client/login")
    public ResponseEntity<AuthResponse> loginClient(@RequestBody AuthRequest request) {
        return ResponseEntity.ok(authService.loginClient(request));
    }

    // Login pour les utilisateurs internes : admin, commercial, caissier
    @PostMapping("/internal/login")
    public ResponseEntity<AuthResponse> loginInternal(@RequestBody AuthRequest request) {
        return ResponseEntity.ok(authService.loginInternal(request));
    }

    // Enregistrement des clients uniquement
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("/reset-initial-password")
    public ResponseEntity<Void> resetInitialPassword(
            @Valid @RequestBody PasswordResetRequest request,
            Authentication authentication // JWT est déjà décodé ici
    ) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).build();
        }

        // ✅ Récupération de l'email depuis le JWT
        String email = authentication.getName(); // c'est l'email du user
        String newPassword = request.newPassword();

        // Appel du service avec email au lieu de l'ID
        adminUserService.performInitialPasswordResetByEmail(email, newPassword);

        return ResponseEntity.ok().build();
    }

}
