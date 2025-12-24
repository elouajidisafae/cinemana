package com.example.cinimana.dto.commercial.response;

import com.example.cinimana.model.StatutReservation;
import java.time.LocalDateTime;

/**
 * DTO simplifié pour afficher les réservations d'une séance
 */
public record ReservationSimpleDTO(
        Long id,
        int nombrePlace,
        LocalDateTime dateReservation,
        StatutReservation statut,
        double montantTotal,
        String ticketPdfUrl,
        LocalDateTime dateValidation,
        // Info client
        Long clientId,
        String clientNom,
        String clientPrenom,
        String clientEmail,
        // Info séance
        Long seanceId,
        LocalDateTime seanceDateHeure,
        String filmTitre) {
}
