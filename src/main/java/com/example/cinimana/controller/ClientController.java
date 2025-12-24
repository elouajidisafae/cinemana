package com.example.cinimana.controller;

import com.example.cinimana.dto.request.ChangePasswordRequest;
import com.example.cinimana.dto.request.ClientProfileRequest;

import com.example.cinimana.model.Reservation;
import com.example.cinimana.service.client.ClientService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import com.example.cinimana.dto.response.ReservationResponse;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/client")
@RequiredArgsConstructor
@CrossOrigin(origins = { "http://localhost:5173", "http://localhost:3000",
        "http://localhost:5174" }, allowCredentials = "true")
public class ClientController {

    private final ClientService clientService;
    private final com.example.cinimana.service.ReservationService reservationService;
    private final com.example.cinimana.service.PDFService pdfService;

    @GetMapping("/reservations")
    public ResponseEntity<List<ReservationResponse>> getMyReservations(
            @AuthenticationPrincipal UserDetails userDetails) {
        List<Reservation> reservations = clientService.getMyReservations(userDetails.getUsername());
        List<ReservationResponse> response = reservations.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/profile")
    public ResponseEntity<com.example.cinimana.dto.response.ClientProfileResponse> getProfile(
            @AuthenticationPrincipal UserDetails userDetails) {
        var client = clientService.getProfile(userDetails.getUsername());
        var response = com.example.cinimana.dto.response.ClientProfileResponse.builder()
                .nom(client.getNom())
                .prenom(client.getPrenom())
                .email(client.getEmail())
                .numeroTelephone(client.getNumeroTelephone())
                .dateNaissance(client.getDateNaissance())
                .build();
        return ResponseEntity.ok(response);
    }

    @PutMapping("/profile")
    public ResponseEntity<?> updateProfile(@AuthenticationPrincipal UserDetails userDetails,
                                           @RequestBody ClientProfileRequest request) {
        return ResponseEntity.ok(clientService.updateProfile(userDetails.getUsername(), request));
    }

    @PutMapping("/password")
    public ResponseEntity<?> updatePassword(@AuthenticationPrincipal UserDetails userDetails,
                                            @RequestBody ChangePasswordRequest request) {
        clientService.updatePassword(userDetails.getUsername(), request);
        return ResponseEntity.ok().build();
    }

    // --- RÉSERVATIONS ---

    @PostMapping("/reservations")
    public ResponseEntity<ReservationResponse> createReservation(@AuthenticationPrincipal UserDetails userDetails,
                                                                 @RequestBody com.example.cinimana.dto.request.ReservationRequest request) {
        Reservation reservation = reservationService.createReservation(userDetails.getUsername(), request);
        return ResponseEntity.ok(mapToResponse(reservation));
    }

    @PutMapping("/reservations/{code}/confirm-presence")
    public ResponseEntity<?> confirmPresence(@PathVariable String code) {
        reservationService.confirmPresence(code);
        return ResponseEntity.ok().build();
    }

    // Endpoint pour le lien envoyé par email (redirige ou confirme simple)
    // GET /api/client/reservations/{code}/confirm
    @GetMapping("/reservations/{code}/confirm")
    public ResponseEntity<?> confirmReservationLink(@PathVariable String code) {
        reservationService.confirmPresence(code);
        return ResponseEntity.ok("Réservation confirmée avec succès !");
    }

    @GetMapping("/reservations/{code}/ticket.pdf")
    public ResponseEntity<byte[]> getTicketPdf(@PathVariable String code) {
        Reservation reservation = reservationService.getReservationByCode(code);

        byte[] pdfBytes = null;

        // 1. Essayer de lire le fichier depuis le chemin stocké
        if (reservation.getTicketPdfPath() != null) {
            try {
                java.nio.file.Path path = java.nio.file.Paths.get(reservation.getTicketPdfPath());
                if (java.nio.file.Files.exists(path)) {
                    pdfBytes = java.nio.file.Files.readAllBytes(path);
                }
            } catch (java.io.IOException e) {
                // Log error
                System.err.println("Erreur lecture PDF stocké: " + e.getMessage());
                // Fallback will generate it
            }
        }

        // 2. Fallback: générer à la volée
        if (pdfBytes == null || pdfBytes.length == 0) {
            try {
                pdfBytes = pdfService.generateReservationTicket(reservation);
            } catch (Exception e) {
                throw new RuntimeException("Erreur génération PDF à la volée: " + e.getMessage(), e);
            }
        }

        return ResponseEntity.ok()
                .header(org.springframework.http.HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=ticket-" + code + ".pdf")
                .contentType(org.springframework.http.MediaType.APPLICATION_PDF)
                .body(pdfBytes);
    }

    // Mapper simple Entity -> DTO
    private ReservationResponse mapToResponse(Reservation reservation) {
        return ReservationResponse.builder()
                .id(reservation.getId())
                .codeReservation(reservation.getCodeReservation())
                .dateReservation(reservation.getDateReservation())
                .statut(reservation.getStatut())
                .montantTotal(reservation.getMontantTotal())
                .nombrePlace(reservation.getNombrePlace())
                .ticketPdfUrl(reservation.getTicketPdfUrl())
                // Infos Seance
                .filmTitre(reservation.getSeance().getFilm().getTitre())
                .filmAfficheUrl(reservation.getSeance().getFilm().getAfficheUrl())
                .seanceDateHeure(reservation.getSeance().getDateHeure())
                .salleNom(reservation.getSeance().getSalle().getNom())
                // Infos Sieges
                .sieges(reservation.getSieges().stream()
                        .map(s -> ReservationResponse.SiegeInfo.builder()
                                .rangee(s.getRangee())
                                .numero(s.getNumero())
                                .build())
                        .collect(Collectors.toList()))
                .build();
    }
}
