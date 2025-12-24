package com.example.cinimana.service;

import com.example.cinimana.dto.request.ReservationRequest;
import com.example.cinimana.dto.request.SiegeRequest;

import com.example.cinimana.model.*;
import com.example.cinimana.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final SeanceRepository seanceRepository;
    private final ClientRepository clientRepository;
    private final OffreRepository offreRepository;
    // Snacks removed
    private final SiegeReserveRepository siegeReserveRepository;
    // SnackReservation removed
    // SnackReservation removed
    private final EmailService emailService;
    private final PDFService pdfService;

    @Value("${app.frontend.url:http://localhost:5173}")
    private String frontendUrl;

    @Transactional
    public Reservation createReservation(String clientEmail, ReservationRequest request) {
        // 1. Récupérer le client
        Client client = clientRepository.findByEmail(clientEmail)
                .orElseThrow(() -> new RuntimeException("Client non trouvé"));

        // 2. Récupérer et valider la séance
        Seance seance = seanceRepository.findById(request.getSeanceId())
                .orElseThrow(() -> new RuntimeException("Séance non trouvée"));

        // Règle : Réservation possible uniquement si > 4h avant
        if (seance.getDateHeure().isBefore(LocalDateTime.now().plusHours(4))) {
            throw new RuntimeException(
                    "Les réservations ne sont plus possibles pour cette séance (délai de 4h dépassé)");
        }

        // 3. Valider la disponibilité des sièges
        for (SiegeRequest siegeReq : request.getSieges()) {
            boolean isReserved = siegeReserveRepository.isSeatReserved(
                    seance.getId(),
                    siegeReq.getRangee(),
                    siegeReq.getNumero());
            if (isReserved) {
                throw new RuntimeException("Le siège Rangée " + siegeReq.getRangee() + " Numéro " + siegeReq.getNumero()
                        + " est déjà réservé");
            }
        }

        // 4. Créer la réservation
        Reservation reservation = new Reservation();
        reservation.setClient(client);
        reservation.setSeance(seance);
        reservation.setNombrePlace(request.getNombrePlaces());
        reservation.setDateReservation(LocalDateTime.now());
        reservation.setStatut(StatutReservation.EN_ATTENTE);

        // 5. Appliquer l'offre si présente
        double prixTicket = seance.getPrixTicket();
        if (request.getOffreId() != null) {
            Offre offre = offreRepository.findById(request.getOffreId())
                    .orElseThrow(() -> new RuntimeException("Offre non trouvée"));

            // Vérifier validité offre (dates)
            LocalDate today = LocalDate.now();
            if (offre.getDateDebut() != null && today.isBefore(offre.getDateDebut())) {
                throw new RuntimeException("L'offre n'est pas encore active");
            }
            if (offre.getDateFin() != null && today.isAfter(offre.getDateFin())) {
                throw new RuntimeException("L'offre a expiré");
            }

            // Règle métier : Les offres ne s'appliquent qu'à la catégorie "Standard"
            if (!"Standard".equalsIgnoreCase(seance.getCategorie().getNom())) {
                throw new RuntimeException("Les offres ne sont applicables que pour la catégorie Standard");
            }

            reservation.setOffre(offre);
            // Pas d'upload de preuve, le client doit la présenter sur place

            // Appliquer réduction
            if (offre.getPrix() > 0) {
                prixTicket = Math.min(prixTicket, offre.getPrix());
            }
        }

        double montantTotalSpectateurs = prixTicket * request.getNombrePlaces();

        // --- SNACKS SUPPRIMÉS DE LA RÉSERVATION ---

        reservation.setMontantTotal(montantTotalSpectateurs);

        // Sauvegarder la réservation pour générer l'ID et le Code UUID
        reservation = reservationRepository.save(reservation);

        // 7. Sauvegarder les sièges
        List<SiegeReserve> sieges = new ArrayList<>();
        for (SiegeRequest siegeReq : request.getSieges()) {
            SiegeReserve siege = new SiegeReserve();
            siege.setReservation(reservation);
            siege.setRangee(siegeReq.getRangee());
            siege.setNumero(siegeReq.getNumero());
            sieges.add(siegeReserveRepository.save(siege));
        }
        reservation.setSieges(sieges);

        // --- SNACKS SUPPRIMÉS DE LA RÉSERVATION (Logique de sauvegarde retirée) ---

        // 9. Générer URL PDF et STOCKER LE CONTENU PDF EN BASE
        reservation.setTicketPdfUrl("/api/client/reservations/" + reservation.getCodeReservation() + "/ticket.pdf");

        // Générer le PDF maintenant
        byte[] pdfContent = null;
        try {
            pdfContent = pdfService.generateReservationTicket(reservation);

            // Définir le répertoire de stockage
            String uploadDir = "tickets";
            java.nio.file.Path uploadPath = java.nio.file.Paths.get(uploadDir);
            if (!java.nio.file.Files.exists(uploadPath)) {
                java.nio.file.Files.createDirectories(uploadPath);
            }

            // Nom du fichier unique
            String fileName = "ticket_" + reservation.getCodeReservation() + ".pdf";
            java.nio.file.Path filePath = uploadPath.resolve(fileName);

            // Écriture du fichier sur le disque
            java.nio.file.Files.write(filePath, pdfContent);

            // Stocker le chemin relatif dans la base
            reservation.setTicketPdfPath(filePath.toString());

        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de la génération/stockage du PDF: " + e.getMessage(), e);
        }

        reservation = reservationRepository.save(reservation);

        // 10. Envoyer email de succès avec le billet PDF en pièce jointe
        String clientName = client.getPrenom() + " " + client.getNom();
        String seanceDateTime = seance.getDateHeure()
                .format(java.time.format.DateTimeFormatter.ofPattern("dd MMMM yyyy 'à' HH:mm"));

        emailService.sendReservationSuccessEmailWithAttachment(
                client.getEmail(),
                clientName,
                seance.getFilm().getTitre(),
                seanceDateTime,
                reservation.getCodeReservation(),
                pdfContent);

        return reservation;
    }

    @Transactional
    public void confirmPresence(String codeReservation) {
        Reservation reservation = reservationRepository.findByCodeReservation(codeReservation);
        if (reservation == null) {
            throw new RuntimeException("Réservation non trouvée");
        }

        if (reservation.getStatut() == StatutReservation.ANNULEE) {
            throw new RuntimeException("Cette réservation a été annulée");
        }

        reservation.setStatut(StatutReservation.CONFIRMEE_CLIENT);
        reservation.setDateConfirmationClient(LocalDateTime.now());
        reservationRepository.save(reservation);
    }

    @Transactional(readOnly = true)
    public Reservation getReservationByCode(String codeReservation) {
        Reservation reservation = reservationRepository.findByCodeReservation(codeReservation);
        if (reservation == null) {
            throw new RuntimeException("Réservation non trouvée");
        }
        // Force initialization of lazy collections while session is open
        org.hibernate.Hibernate.initialize(reservation.getSieges());
        org.hibernate.Hibernate.initialize(reservation.getSeance().getFilm());
        org.hibernate.Hibernate.initialize(reservation.getSeance().getSalle());
        org.hibernate.Hibernate.initialize(reservation.getSeance().getCategorie());
        return reservation;
    }
}
