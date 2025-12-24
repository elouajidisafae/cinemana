package com.example.cinimana.dto.commercial.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import org.springframework.format.annotation.DateTimeFormat;

/**
 * DTO pour la création et modification d'une séance par un Commercial
 */
public record SeanceRequestDTO(
        @NotNull(message = "La date et l'heure sont obligatoires") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateHeure,

        Double prixTicket,

        @NotBlank(message = "L'ID du film est obligatoire") String filmId,

        @NotBlank(message = "L'ID de la salle est obligatoire") String salleId,

        @NotNull(message = "L'ID de la catégorie est obligatoire") Long categorieId) {
}
