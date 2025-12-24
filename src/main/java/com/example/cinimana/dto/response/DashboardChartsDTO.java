package com.example.cinimana.dto.response;

import java.util.List;
import java.util.Map;

public record DashboardChartsDTO(
        Map<String, Long> reservationStatus,
        List<TopFilmDTO> topFilms,
        Map<Integer, Long> peakHours,
        Map<String, Double> dailyRevenue,
        Map<String, Long> genreDistribution,
        Map<String, Map<String, Long>> dailyStatusStats) {
    public record TopFilmDTO(String titre, Long count) {
    }
}
