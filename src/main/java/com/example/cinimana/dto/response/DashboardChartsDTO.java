package com.example.cinimana.dto.response;

import java.util.List;
import java.util.Map;

public record DashboardChartsDTO(
        Map<String, Long> reservationStatus,
        List<TopFilmDTO> topFilms,
        Map<Integer, Long> peakHours) {
    public record TopFilmDTO(String titre, Long count) {
    }
}
