package com.example.cinimana.dto.commercial.response;

import com.example.cinimana.model.TypeOperation;
import java.time.LocalDateTime;

public record HistoriqueSeanceDTO(
        Long id,
        TypeOperation operation,
        LocalDateTime dateOperation,
        Long seanceId,
        String filmTitre,
        String salleNom) {
}
