package com.example.cinimana.dto.response;

import java.time.LocalDateTime;

public record SeanceResponseDTO(
        Long id,
        LocalDateTime dateHeure,
        double prixTicket,
        String salleNom,
        int nombreRangees,
        int siegesParRangee,
        String categorieNom,
        int placesDisponibles,
        String filmId) {
}
