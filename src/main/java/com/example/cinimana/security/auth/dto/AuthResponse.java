package com.example.cinimana.security.auth.dto;

public record AuthResponse(
        String token,
        String role,
        String nomComplet,
        String email,
        String id,
        boolean premiereConnexion,
        String nom,
        String prenom
) {
}