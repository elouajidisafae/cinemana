package com.example.cinimana.security.auth.dto;

public record AuthResponse(
        String token,
        String role,
        String nomComplet,
        String email, // ✅ Ajout du email
        String id, // ✅ CORRECTION : Doit être de type String
        boolean premiereConnexion // ✅ Nouveau champ
) {
}