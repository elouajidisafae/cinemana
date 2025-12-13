package com.example.cinimana.dto.response;
import java.time.LocalDateTime;

public record HistoriqueResponseDTO(
        Long idOperation,
        String entiteType,
        String entiteNom,
        String operation,
        LocalDateTime dateOperation,
        String adminNomComplet
) {}