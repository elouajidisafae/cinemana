package com.example.cinimana.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class Reservation extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String codeReservation; // UUID pour QR code et identification

    @Column(nullable = false)
    private Integer nombrePlace;

    @Column(nullable = false)
    private LocalDateTime dateReservation;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private StatutReservation statut;

    private String ticketPdfUrl; // URL d'accès au ticket PDF (ex: "/api/tickets/{codeReservation}")

    @Column(nullable = false)
    private Double montantTotal;

    @Column(name = "ticket_pdf_path")
    private String ticketPdfPath; // Chemin du fichier PDF stocké sur disque (ex: "tickets/ticket-123.pdf")

    // Relations
    @ManyToOne
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;

    @ManyToOne
    @JoinColumn(name = "seance_id", nullable = false)
    private Seance seance;

    @ManyToOne
    @JoinColumn(name = "caissier_id")
    private Caissier caissier;

    @ManyToOne
    @JoinColumn(name = "offre_id")
    private Offre offre; // Offre appliquée (optionnelle)

    // Nouvelles relations
    @OneToMany(mappedBy = "reservation", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SiegeReserve> sieges = new ArrayList<>();

    // Snacks supprimés

    // Timestamps pour le système de confirmation
    private LocalDateTime dateConfirmationEmail; // Quand l'email de confirmation a été envoyé (3h avant)
    private LocalDateTime dateConfirmationClient; // Quand le client a confirmé sa présence
    private LocalDateTime dateValidation; // Quand la réservation a été validée à la caisse

    // Preuve étudiant (si offre étudiant appliquée)
    // NOTE: Le client doit présenter sa carte à l'entrée, pas d'upload nécessaire.

    @PrePersist
    public void prePersist() {
        if (dateReservation == null) {
            dateReservation = LocalDateTime.now();
        }
        if (statut == null) {
            statut = StatutReservation.EN_ATTENTE;
        }
        if (codeReservation == null) {
            codeReservation = UUID.randomUUID().toString();
        }
    }

    // Helper methods
    public void addSiege(SiegeReserve siege) {
        sieges.add(siege);
        siege.setReservation(this);
    }

}
