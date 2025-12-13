// src/main/java/com/example/cinimana/dto/response/DashboardStatsDTO.java
package com.example.cinimana.dto.response;

public record DashboardStatsDTO(
        long totalComptesActifs,
        long totalComptesInactifs,
        long totalCommerciauxActifs,
        long totalCaissiersActifs,
        long totalUtilisateurs,
        long totalAjouteParAdminCourant,
        long totalClientsCrees
) {}