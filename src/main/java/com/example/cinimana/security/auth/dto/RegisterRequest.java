package com.example.cinimana.security.auth.dto;

import java.time.LocalDate;

public record RegisterRequest(
        String nom,
        String prenom,
        String email,
        String motDePasse,
        String numeroTelephone,
        LocalDate dateNaissance
) {}