package com.example.cinimana.dto.commercial.response;

/**
 * DTO pour les statistiques d'une séance
 */
public record SeanceStatsDTO(
        Long seanceId,
        int nombreReservations,
        int totalPlacesReservees,
        double tauxRemplissage,
        double revenuTotal,
        int nombreReservationsEnAttente,
        int nombreReservationsValidees,
        int nombreReservationsAnnulees,
        int placesDisponibles,
        int capaciteTotale) {
}
