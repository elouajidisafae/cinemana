package com.example.cinimana.dto.response;

import java.time.LocalDate;

public record FilmResponseDTO(
        String id,
        String titre,
        String description,
        int duree,
        String genre,
        LocalDate dateSortie,
        String afficheUrl,
        String trailerUrl,
        String ageLimite,
        boolean actif // Statut Soft Delete pour l'affichage Admin
) {
}