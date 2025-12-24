package com.example.cinimana.dto.commercial.response;

import java.time.LocalDate;

/**
 * DTO pour les statistiques journalières (pour les graphiques)
 */
public record DailyStatsDTO(
        LocalDate date,
        int nombreSeances,
        int nombreReservations,
        double revenu) {
}
