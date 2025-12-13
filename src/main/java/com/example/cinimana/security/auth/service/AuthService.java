// src/main/java/com/example/cinimana/security/auth/service/AuthService.java
package com.example.cinimana.security.auth.service;

import com.example.cinimana.security.auth.dto.AuthRequest;
import com.example.cinimana.security.auth.dto.AuthResponse;
import com.example.cinimana.security.auth.dto.RegisterRequest;

public interface AuthService {
    // Login client
    AuthResponse loginClient(AuthRequest request);

    // Login utilisateurs internes : admin, commercial, caissier
    AuthResponse loginInternal(AuthRequest request);

    // Register client
    AuthResponse register(RegisterRequest request);
}