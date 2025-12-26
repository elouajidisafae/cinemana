package com.example.cinimana.service.caissier;

import com.example.cinimana.model.Caissier;
import com.example.cinimana.model.Reservation;
import com.example.cinimana.model.StatutReservation;
import com.example.cinimana.repository.CaissierRepository;
import com.example.cinimana.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CaissierService {

    private static final Logger logger = LoggerFactory.getLogger(CaissierService.class);

    private final ReservationRepository reservationRepo;
    private final CaissierRepository caissierRepo;

    /**
     * ÉTAPE 1 : VÉRIFIER (sans modifier la BDD)
     */
    public Map<String, Object> verifierBillet(String code) {
        logger.info("Vérification du billet avec le code: {}", code);
        Map<String, Object> response = new HashMap<>();

        // 1. Chercher la réservation
        Reservation resa = reservationRepo.findByCodeReservation(code);

        if (resa == null) {
            response.put("success", false);
            response.put("canValidate", false);
            response.put("message", "❌ Réservation introuvable");
            response.put("errorType", "NOT_FOUND");
            return response;
        }

        // 2. Vérifier le statut
        if (resa.getStatut() == StatutReservation.VALIDEE) {
            response.put("success", false);
            response.put("canValidate", false);
            response.put("message", "❌ Billet déjà utilisé");
            response.put("errorType", "ALREADY_VALIDATED");
            response.put("dateValidation", resa.getDateValidation());
            if (resa.getCaissier() != null) {
                response.put("validePar", resa.getCaissier().getNom() + " " + resa.getCaissier().getPrenom());
            }
            addReservationInfo(response, resa);
            return response;
        }

        if (resa.getStatut() == StatutReservation.ANNULEE) {
            response.put("success", false);
            response.put("canValidate", false);
            response.put("message", "❌ Réservation annulée");
            response.put("errorType", "CANCELLED");
            addReservationInfo(response, resa);
            return response;
        }

        // 3. Vérifier l'horaire de la séance
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime seanceDebut = resa.getSeance().getDateHeure();

        String warningMessage = null;

        if (now.isBefore(seanceDebut.minusMinutes(30))) {
            warningMessage = "⚠️ Attention : Trop tôt ! La séance commence à " +
                    seanceDebut.toLocalTime();
        }

        if (now.isAfter(seanceDebut.plusMinutes(60))) { // Warning deeper into the session
            warningMessage = "⚠️ Attention : La séance a commencé depuis plus d'une heure (début à " +
                    seanceDebut.toLocalTime() + ")";
        }

        // 4. Tout est OK - Renvoyer les infos
        response.put("success", true);
        response.put("canValidate", true);
        response.put("message", "✅ Billet valide - Prêt à valider");
        response.put("reservationId", resa.getId());
        response.put("code", resa.getCodeReservation());

        if (warningMessage != null) {
            response.put("warning", warningMessage);
        }

        addReservationInfo(response, resa);

        return response;
    }

    /**
     * ÉTAPE 2 : VALIDER définitivement
     */
    @Transactional
    public Map<String, Object> validerEntree(Long reservationId, String username) {
        logger.info("Validation de l'entrée pour la réservation ID: {} par {}", reservationId, username);
        Map<String, Object> response = new HashMap<>();

        // 1. Récupérer la réservation
        Optional<Reservation> optResa = reservationRepo.findById(reservationId);

        if (optResa.isEmpty()) {
            response.put("success", false);
            response.put("message", "❌ Réservation introuvable");
            return response;
        }

        Reservation resa = optResa.get();

        // 2. Double vérification
        if (resa.getStatut() == StatutReservation.VALIDEE) {
            response.put("success", false);
            response.put("message", "❌ Déjà validé par " +
                    (resa.getCaissier() != null ? (resa.getCaissier().getNom() + " " + resa.getCaissier().getPrenom())
                            : "un autre caissier"));
            return response;
        }

        // 3. VALIDATION DÉFINITIVE
        resa.setStatut(StatutReservation.VALIDEE);
        resa.setDateValidation(LocalDateTime.now());

        // Associer le caissier
        if (username != null) {
            Optional<Caissier> caissier = caissierRepo.findByEmail(username);
            caissier.ifPresent(resa::setCaissier);
        }

        reservationRepo.save(resa);

        // 4. Confirmation
        response.put("success", true);
        response.put("message", "✅ Entrée validée avec succès !");
        response.put("validationTime", resa.getDateValidation());

        return response;
    }

    /**
     * ÉTAPE 3 : ANNULER la réservation
     */
    @Transactional
    public Map<String, Object> annulerEntree(Long reservationId, String username) {
        logger.info("Annulation de l'entrée pour la réservation ID: {} par {}", reservationId, username);
        Map<String, Object> response = new HashMap<>();

        // 1. Récupérer la réservation
        Optional<Reservation> optResa = reservationRepo.findById(reservationId);

        if (optResa.isEmpty()) {
            response.put("success", false);
            response.put("message", "❌ Réservation introuvable");
            return response;
        }

        Reservation resa = optResa.get();

        // 2. Vérifier si déjà annulée
        if (resa.getStatut() == StatutReservation.ANNULEE) {
            response.put("success", false);
            response.put("message", "⚠️ Déjà annulée");
            return response;
        }

        // 3. ANNULATION
        resa.setStatut(StatutReservation.ANNULEE);

        // Associer le caissier qui annule
        if (username != null) {
            Optional<Caissier> caissier = caissierRepo.findByEmail(username);
            caissier.ifPresent(resa::setCaissier);
        }

        reservationRepo.save(resa);

        // 4. Confirmation
        response.put("success", true);
        response.put("message", "🚫 Réservation annulée avec succès");

        return response;
    }

    /**
     * Stats du Caissier (et Activité Récente)
     */
    public Map<String, Object> getStats(String email) {
        Map<String, Object> stats = new HashMap<>();
        LocalDate today = LocalDate.now();
        LocalDateTime debut = today.minusDays(7).atStartOfDay();
        LocalDateTime fin = today.atTime(23, 59, 59);

        // Find Caissier
        Optional<Caissier> caissierOpt = caissierRepo.findByEmail(email);

        if (caissierOpt.isEmpty()) {
            // Return empty stats if not found (should not happen if auth works)
            stats.put("totalValidations", 0L);
            stats.put("totalPlaces", 0);
            stats.put("totalMontant", 0.0);
            stats.put("recentActivity", List.of());
            return stats;
        }

        Caissier caissier = caissierOpt.get();

        // 1. Chiffres du jour (Filtered by Caissier)
        long totalValidations = reservationRepo.countByStatutAndCaissierAndDateValidationBetween(
                StatutReservation.VALIDEE, caissier, debut, fin);

        Integer totalPlaces = reservationRepo.sumNombrePlacesByStatutAndCaissierAndDateValidationBetween(
                StatutReservation.VALIDEE, caissier, debut, fin);

        Double totalMontant = reservationRepo.sumMontantByStatutAndCaissierAndDateValidationBetween(
                StatutReservation.VALIDEE, caissier, debut, fin);

        stats.put("totalValidations", totalValidations);
        stats.put("totalPlaces", totalPlaces != null ? totalPlaces : 0);
        stats.put("totalMontant", totalMontant != null ? totalMontant : 0.0);

        // 2. Activité Récente (Filtered by Caissier)
        List<Reservation> recentReservations = reservationRepo
                .findTop5ByStatutAndCaissierOrderByDateValidationDesc(StatutReservation.VALIDEE, caissier);

        List<Map<String, Object>> recentActivity = recentReservations.stream().map(r -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", r.getId());
            map.put("client", r.getClient().getNom() + " " + r.getClient().getPrenom());
            map.put("film", r.getSeance().getFilm().getTitre());
            map.put("places", r.getNombrePlace());
            map.put("montant", r.getMontantTotal());
            map.put("heure", r.getDateValidation());
            return map;
        }).toList();

        stats.put("recentActivity", recentActivity);

        return stats;
    }

    private void addReservationInfo(Map<String, Object> response, Reservation resa) {
        response.put("client", resa.getClient().getNom() + " " + resa.getClient().getPrenom());
        response.put("clientEmail", resa.getClient().getEmail());
        response.put("film", resa.getSeance().getFilm().getTitre());
        response.put("horaire", resa.getSeance().getDateHeure().toLocalTime().toString());
        response.put("dateSeance", resa.getSeance().getDateHeure().toLocalDate().toString());
        response.put("salle", resa.getSeance().getSalle().getNom());
        response.put("places", resa.getNombrePlace());
        response.put("montant", resa.getMontantTotal());
        response.put("dateReservation", resa.getDateReservation());
        response.put("statutActuel", resa.getStatut());

        // Liste des sièges
        List<String> siegeStrList = resa.getSieges().stream()
                .map(s -> "R" + s.getRangee() + "-N" + s.getNumero())
                .toList();
        response.put("sieges", siegeStrList);

        // Détails de l'offre
        if (resa.getOffre() != null) {
            Map<String, Object> offreInfo = new HashMap<>();
            offreInfo.put("titre", resa.getOffre().getTitre());
            offreInfo.put("prixApplique", resa.getOffre().getPrix());
            response.put("offre", offreInfo);
        }

        // Intégration de la catégorie comme demandé
        if (resa.getSeance().getCategorie() != null) {
            response.put("categorie", resa.getSeance().getCategorie().getNom());
            response.put("prixUnitaire", resa.getSeance().getCategorie().getPrixBase());
        } else {
            response.put("categorie", "Standard");
            response.put("prixUnitaire", 0.0);
        }
    }
}
