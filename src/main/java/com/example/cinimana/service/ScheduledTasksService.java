package com.example.cinimana.service;

import com.example.cinimana.model.Reservation;
import com.example.cinimana.model.StatutReservation;
import com.example.cinimana.repository.ReservationRepository;
import com.example.cinimana.repository.OffreRepository;
import com.example.cinimana.model.Offre;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ScheduledTasksService {
    //
    private static final Logger logger = LoggerFactory.getLogger(ScheduledTasksService.class);
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd MMMM yyyy à HH:mm");

    private final ReservationRepository reservationRepository;
    private final OffreRepository offreRepository;
    private final EmailService emailService;

    @Value("${app.frontend.url:http://localhost:5173}")
    private String frontendUrl;

    /**
     * Tâche planifiée : Envoyer les emails de confirmation 3h avant la séance
     * S'exécute toutes les 10 minutes
     */
    @Scheduled(fixedRate = 600000) // 10 minutes = 600000 ms
    @Transactional
    public void sendConfirmationEmails() {
        logger.info("🔄 Démarrage de la tâche d'envoi d'emails de confirmation...");

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime threeHoursLater = now.plusHours(3);
        LocalDateTime threeHoursAndTenMinutesLater = now.plusHours(3).plusMinutes(10);

        // Trouver les réservations dont la séance commence dans 3h (±10 min)
        List<Reservation> reservations = reservationRepository
                .findReservationsNeedingConfirmationEmail(threeHoursLater, threeHoursAndTenMinutesLater);

        logger.info("📧 {} réservation(s) nécessitent un email de confirmation", reservations.size());

        int successCount = 0;
        int errorCount = 0;

        for (Reservation reservation : reservations) {
            try {
                // Construire le lien de confirmation
                String confirmationLink = String.format(
                        "%s/client/reservations/confirm/%s",
                        frontendUrl,
                        reservation.getCodeReservation());

                // Envoyer l'email
                String clientName = reservation.getClient().getPrenom() + " " + reservation.getClient().getNom();
                emailService.sendReservationConfirmationEmail(
                        reservation.getClient().getEmail(),
                        clientName,
                        reservation.getSeance().getFilm().getTitre(),
                        reservation.getSeance().getDateHeure().format(DATE_TIME_FORMATTER),
                        reservation.getSeance().getSalle().getNom(),
                        reservation.getNombrePlace(),
                        reservation.getCodeReservation(),
                        confirmationLink);

                // Mettre à jour la date d'envoi de l'email
                reservation.setDateConfirmationEmail(now);
                reservationRepository.save(reservation);

                successCount++;
                logger.info("✅ Email envoyé pour réservation: {}", reservation.getCodeReservation());

            } catch (Exception e) {
                errorCount++;
                logger.error("❌ Erreur envoi email pour réservation {}: {}",
                        reservation.getCodeReservation(), e.getMessage());
            }
        }

        logger.info("✅ Tâche terminée: {} succès, {} erreurs", successCount, errorCount);
    }

    /**
     * Tâche planifiée : Annuler les réservations non confirmées
     * S'exécute toutes les 5 minutes
     */
    @Scheduled(fixedRate = 300000) // 5 minutes = 300000 ms
    @Transactional
    public void cancelUnconfirmedReservations() {
        logger.info("🔄 Démarrage de la tâche d'annulation des réservations non confirmées...");

        LocalDateTime oneHourAgo = LocalDateTime.now().minusHours(1);

        // Trouver les réservations dont l'email a été envoyé il y a plus d'1h
        // et qui n'ont pas été confirmées par le client
        List<Reservation> reservationsToCancel = reservationRepository
                .findReservationsToCancel(oneHourAgo);

        logger.info("🚫 {} réservation(s) à annuler", reservationsToCancel.size());

        int cancelledCount = 0;

        for (Reservation reservation : reservationsToCancel) {
            try {
                reservation.setStatut(StatutReservation.ANNULEE);
                reservationRepository.save(reservation);

                cancelledCount++;
                logger.info("🚫 Réservation annulée: {} (non confirmée dans le délai)",
                        reservation.getCodeReservation());

                // Optionnel : Envoyer un email d'annulation au client
                // emailService.sendCancellationEmail(...);

            } catch (Exception e) {
                logger.error("❌ Erreur annulation réservation {}: {}",
                        reservation.getCodeReservation(), e.getMessage());
            }
        }

        logger.info("✅ Tâche terminée: {} réservation(s) annulée(s)", cancelledCount);
    }

    /**
     * Tâche planifiée : Nettoyer les anciens billets PDF du disque
     * S'exécute tous les jours à 3h du matin
     */
    @Scheduled(cron = "0 0 3 * * *")
    public void cleanupOldTicketFiles() {
        logger.info("🧹 Démarrage du nettoyage des anciens billets PDF sur le disque...");

        try {
            java.nio.file.Path uploadPath = java.nio.file.Paths.get("tickets");
            if (!java.nio.file.Files.exists(uploadPath))
                return;

            long now = System.currentTimeMillis();
            long thirtyDaysMs = 30L * 24 * 60 * 60 * 1000;

            java.nio.file.Files.list(uploadPath).forEach(path -> {
                try {
                    long lastModified = java.nio.file.Files.getLastModifiedTime(path).toMillis();
                    if (now - lastModified > thirtyDaysMs) {
                        java.nio.file.Files.delete(path);
                        logger.info("🗑️ Billet supprimé (plus de 30 jours): {}", path.getFileName());
                    }
                } catch (Exception e) {
                    logger.error("❌ Impossible de supprimer le fichier {}: {}", path, e.getMessage());
                }
            });

        } catch (Exception e) {
            logger.error("❌ Erreur lors du nettoyage du dossier tickets: {}", e.getMessage());
        }

        logger.info("✅ Nettoyage des billets terminé");
    }

    /**
     * Tâche planifiée : Nettoyer les anciennes réservations (optionnel)
     * S'exécute tous les jours à 2h du matin
     */
    @Scheduled(cron = "0 0 2 * * *")
    @Transactional
    public void cleanupOldReservations() {
        logger.info("🧹 Démarrage du nettoyage des anciennes réservations...");

        // Exemple : Supprimer les réservations annulées de plus de 30 jours
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);

        // Cette logique peut être ajustée selon vos besoins
        // Par exemple, archiver au lieu de supprimer

        logger.info("✅ Nettoyage terminé");
    }

    /**
     * Tâche planifiée : Annuler les réservations "No-Show" (Passé 30 min après le
     * début)
     * S'exécute toutes les 15 minutes
     */
    @Scheduled(fixedRate = 900000) // 15 minutes = 900000 ms
    @Transactional
    public void cancelNoShowReservations() {
        logger.info("🔄 Démarrage de la tâche d'annulation des No-Shows (30 min après début)...");

        LocalDateTime threshold = LocalDateTime.now().minusMinutes(30);

        List<Reservation> noShows = reservationRepository.findNoShowReservations(threshold);

        if (!noShows.isEmpty()) {
            logger.info("🚫 {} réservation(s) No-Show détectée(s)", noShows.size());
            for (Reservation reservation : noShows) {
                reservation.setStatut(StatutReservation.ANNULEE);
                reservationRepository.save(reservation);
                logger.info("🚫 Réservation {} annulée (No-Show pour séance de {})",
                        reservation.getCodeReservation(),
                        reservation.getSeance().getDateHeure());
            }
        }

        logger.info("✅ Tâche No-Show terminée");
    }

    /**
     * Tâche planifiée : Désactiver les offres expirées
     * S'exécute chaque jour à minuit
     */
    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void deactivateExpiredOffres() {
        java.time.LocalDate today = java.time.LocalDate.now();
        logger.info("🔄 Vérification des offres expirées... Date: {}", today);

        List<Offre> expiredOffres = offreRepository.findByActifTrueAndDateFinBefore(today);

        if (!expiredOffres.isEmpty()) {
            logger.info("🚫 {} offre(s) expirée(s) détectée(s)", expiredOffres.size());
            for (Offre offre : expiredOffres) {
                offre.setActif(false);
                offreRepository.save(offre);
                logger.info("🚫 Offre '{}' (ID: {}) désactivée", offre.getTitre(), offre.getId());
            }
            offreRepository.flush();
        }
    }
}
