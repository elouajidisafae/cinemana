package com.example.cinimana.dto.commercial.response;

import java.util.List;

/**
 * DTO pour les statistiques globales du dashboard Commercial
 */
public record DashboardStatsDTO(
        long totalSeances,
        long totalReservations,
        double revenuTotal,
        double tauxRemplissageGlobal,
        List<DailyStatsDTO> statsParJour,
        List<SeanceResponseDTO> upcomingSeances,
        List<ReservationSimpleDTO> recentReservations) {
}
