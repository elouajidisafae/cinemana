package com.example.cinimana.dto.commercial.response;

import java.time.LocalDate;

public record FilmSimpleDTO(
        String id,
        String titre,
        String description,
        int duree,
        String genre,
        LocalDate dateSortie,
        String afficheUrl,
        String trailerUrl,
        boolean actif) {
}
