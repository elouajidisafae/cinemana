
package com.example.cinimana.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

public record FilmRequestDTO(
        @NotBlank(message = "Le titre est obligatoire.") String titre,
        @NotBlank(message = "La description est obligatoire.") String description,
        @NotNull(message = "La durée est obligatoire.") @Min(value = 1, message = "La durée doit être supérieure à zéro.") Integer duree, // en
        // minutes
        @NotBlank(message = "Le genre est obligatoire.") String genre,
        @NotNull(message = "La date de sortie est obligatoire.") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateSortie,
        String afficheUrl,
        String trailerUrl,
        String ageLimite) {
}