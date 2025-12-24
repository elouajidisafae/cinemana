package com.example.cinimana.dto.commercial.response;

import java.time.LocalDateTime;

/**
 * DTO pour la réponse contenant les informations complètes d'une séance
 */
public record SeanceResponseDTO(
        Long id,
        LocalDateTime dateHeure,
        double prixTicket,
        int placesReservees,
        int placesDisponibles,
        String filmTitre,
        String filmGenre,
        String filmId,
        String salleNom,
        String salleId,
        int salleCapacite,
        String categorieNom,
        Long categorieId,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        boolean active) {
}
