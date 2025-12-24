package com.example.cinimana.dto.response;

public record SalleResponseDTO(
        String id,
        String nom,
        int capacite,
        String type,
        Integer nombreRangees,
        Integer siegesParRangee,
        boolean actif // Afficher l'état Soft Delete
) {
}