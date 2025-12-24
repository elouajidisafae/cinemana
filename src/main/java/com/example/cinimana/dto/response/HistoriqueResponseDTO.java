package com.example.cinimana.dto.response;

import java.time.LocalDateTime;

public record HistoriqueResponseDTO(
        Long idOperation,
        String entiteType,
        String entiteId,
        String entiteNom,
        String operation,
        LocalDateTime dateOperation,
        String adminNomComplet,
        Double montant,
        String infoSupplementaire) {
}