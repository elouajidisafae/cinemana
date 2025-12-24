package com.example.cinimana.service.commercial;

import com.example.cinimana.dto.commercial.request.SeanceRequestDTO;
import com.example.cinimana.dto.commercial.response.*;
import com.example.cinimana.exception.NotFoundException;
import com.example.cinimana.model.*;
import com.example.cinimana.repository.*;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service pour la gestion des séances par les Commerciaux
 */
@Service
@RequiredArgsConstructor
public class CommercialSeanceService {

    private static final Logger logger = LoggerFactory.getLogger(CommercialSeanceService.class);

    private final SeanceRepository seanceRepository;
    private final FilmRepository filmRepository;
    private final SalleRepository salleRepository;
    private final CategorieRepository categorieRepository;
    private final ReservationRepository reservationRepository;
    private final HistoriqueSeanceRepository historiqueSeanceRepository;
    private final CommercialRepository commercialRepository;

    // ==================== CRUD OPERATIONS ====================

    /**
     * Créer une nouvelle séance
     */
    @Transactional
    public SeanceResponseDTO createSeance(SeanceRequestDTO dto) {
        logger.info("Création d'une nouvelle séance pour le film {} dans la salle {}", dto.filmId(),
                dto.salleId());

        // Validation des entités liées
        Film film = filmRepository.findById(dto.filmId())
                .orElseThrow(() -> new NotFoundException("Film non trouvé avec ID: " + dto.filmId()));

        Salle salle = salleRepository.findById(dto.salleId())
                .orElseThrow(() -> new NotFoundException(
                        "Salle non trouvée avec ID: " + dto.salleId()));

        Categorie categorie = categorieRepository.findById(dto.categorieId())
                .orElseThrow(() -> new NotFoundException(
                        "Catégorie non trouvée avec ID: " + dto.categorieId()));

        // VALIDATION CRITIQUE: Empêcher la création dans le passé
        validateNotPast(dto.dateHeure());

        // VALIDATION CRITIQUE: Vérifier la disponibilité de la salle
        validateSalleAvailability(dto.salleId(), dto.dateHeure(), film.getDuree(), null);

        // Créer la séance
        Seance seance = new Seance();
        seance.setDateHeure(dto.dateHeure());
        seance.setFilm(film);
        seance.setSalle(salle);
        seance.setCategorie(categorie);
        // legacyPrixTicket removed as per user request to not modify models

        Seance savedSeance = seanceRepository.save(seance);

        // Enregistrer dans l'historique
        logHistorique(savedSeance, TypeOperation.CREATION);

        logger.info("Séance créée avec succès: ID {}", savedSeance.getId());
        return mapToSeanceResponseDTO(savedSeance);
    }

    /**
     * Modifier une séance existante
     */
    @Transactional
    public SeanceResponseDTO updateSeance(Long id, SeanceRequestDTO dto) {
        logger.info("Modification de la séance {}", id);

        Seance seance = seanceRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Séance non trouvée avec ID: " + id));

        // VALIDATION CRITIQUE: Vérifier qu'il n'y a pas de réservations validées
        validateNoValidatedReservations(id);

        // Validation des entités liées
        Film film = filmRepository.findById(dto.filmId())
                .orElseThrow(() -> new NotFoundException("Film non trouvé avec ID: " + dto.filmId()));

        Salle salle = salleRepository.findById(dto.salleId())
                .orElseThrow(() -> new NotFoundException(
                        "Salle non trouvée avec ID: " + dto.salleId()));

        Categorie categorie = categorieRepository.findById(dto.categorieId())
                .orElseThrow(() -> new NotFoundException(
                        "Catégorie non trouvée avec ID: " + dto.categorieId()));

        // VALIDATION CRITIQUE: Empêcher le déplacement dans le passé
        validateNotPast(dto.dateHeure());

        // VALIDATION CRITIQUE: Vérifier la disponibilité de la salle (sauf pour la
        // séance actuelle)
        validateSalleAvailability(dto.salleId(), dto.dateHeure(), film.getDuree(), id);

        // Mettre à jour les champs
        seance.setDateHeure(dto.dateHeure());
        seance.setFilm(film);
        seance.setSalle(salle);
        seance.setCategorie(categorie);

        Seance updatedSeance = seanceRepository.save(seance);

        // Enregistrer dans l'historique
        logHistorique(updatedSeance, TypeOperation.MODIFICATION);

        logger.info("Séance modifiée avec succès: ID {}", id);
        return mapToSeanceResponseDTO(updatedSeance);
    }

    /**
     * Consulter une séance par ID
     */
    public SeanceResponseDTO getSeanceById(Long id) {
        Seance seance = seanceRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Séance non trouvée avec ID: " + id));
        return mapToSeanceResponseDTO(seance);
    }

    /**
     * Lister les séances avec filtres optionnels
     */
    @Transactional(readOnly = true)
    public List<SeanceResponseDTO> getSeances(String filmId, String salleId,
                                              LocalDateTime dateDebut, LocalDateTime dateFin) {

        List<Seance> seances;

        if (dateDebut != null && dateFin != null) {
            seances = seanceRepository.findByDateHeureBetween(dateDebut, dateFin);
        } else if (dateDebut != null) {
            seances = seanceRepository.findSeancesDisponibles(dateDebut);
        } else {
            seances = seanceRepository.findAll();
        }

        return seances.stream()
                .filter(s -> (filmId == null || filmId.isEmpty()) || s.getFilm().getId().equals(filmId))
                .filter(s -> (salleId == null || salleId.isEmpty())
                        || s.getSalle().getId().equals(salleId))
                .map(seance -> {
                    try {
                        return mapToSeanceResponseDTO(seance);
                    } catch (Exception e) {
                        logger.error("Erreur de mapping pour la séance ID {}: {}",
                                seance.getId(), e.getMessage());
                        return null;
                    }
                })
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.toList());
    }

    // ==================== RESERVATIONS ====================

    /**
     * Consulter les réservations d'une séance
     */
    public List<ReservationSimpleDTO> getReservationsBySeance(Long seanceId) {
        Seance seance = seanceRepository.findById(seanceId)
                .orElseThrow(() -> new NotFoundException("Séance non trouvée avec ID: " + seanceId));

        return reservationRepository.findBySeance(seance).stream()
                .map(this::mapToReservationSimpleDTO)
                .collect(Collectors.toList());
    }

    /**
     * Lister toutes les réservations (pour le mode Read-Only global)
     */
    public List<ReservationSimpleDTO> getAllReservations() {
        return reservationRepository.findAll().stream()
                .sorted(java.util.Comparator.comparing(Reservation::getDateReservation).reversed())
                .map(this::mapToReservationSimpleDTO)
                .collect(Collectors.toList());
    }

    /**
     * Obtenir les statistiques d'une séance
     */
    public SeanceStatsDTO getSeanceStats(Long seanceId) {
        Seance seance = seanceRepository.findById(seanceId)
                .orElseThrow(() -> new NotFoundException("Séance non trouvée avec ID: " + seanceId));

        List<Reservation> reservations = reservationRepository.findBySeance(seance);

        int nombreReservations = reservations.size();

        int totalPlacesReservees = seance.getSalle().getCapacite() - seance.getPlacesDisponibles();
        int placesDisponibles = seance.getPlacesDisponibles();
        int capaciteTotale = seance.getSalle().getCapacite();

        double tauxRemplissage = capaciteTotale > 0
                ? (double) totalPlacesReservees / capaciteTotale * 100
                : 0.0;

        long nombreReservationsEnAttente = reservations.stream()
                .filter(r -> r.getStatut() == StatutReservation.EN_ATTENTE)
                .count();

        long nombreReservationsValidees = reservations.stream()
                .filter(r -> r.getStatut() == StatutReservation.VALIDEE)
                .count();

        long nombreReservationsAnnulees = reservations.stream()
                .filter(r -> r.getStatut() == StatutReservation.ANNULEE)
                .count();

        double revenuTotal = reservations.stream()
                .filter(r -> r.getStatut() == StatutReservation.VALIDEE)
                .mapToDouble(Reservation::getMontantTotal)
                .sum();

        return new SeanceStatsDTO(
                seanceId,
                nombreReservations,
                totalPlacesReservees,
                tauxRemplissage,
                revenuTotal,
                (int) nombreReservationsEnAttente,
                (int) nombreReservationsValidees,
                (int) nombreReservationsAnnulees,
                placesDisponibles,
                capaciteTotale);
    }

    // ==================== VALIDATIONS ====================

    private void validateNotPast(LocalDateTime dateHeure) {
        if (dateHeure.isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Impossible de programmer une séance dans le passé.");
        }
    }

    private void validateSalleAvailability(String salleId, LocalDateTime dateHeure,
                                           int dureeFilm, Long excludeSeanceId) {
        LocalDateTime finSeance = dateHeure.plusMinutes(dureeFilm);

        Salle salle = salleRepository.findById(salleId)
                .orElseThrow(() -> new NotFoundException("Salle non trouvée: " + salleId));

        List<Seance> seancesExistantes = seanceRepository.findSeancesBySalleAndDateHeureBetween(
                salle.getId(),
                dateHeure.minusHours(5),
                dateHeure.plusHours(5));

        for (Seance s : seancesExistantes) {
            if (excludeSeanceId != null && s.getId().equals(excludeSeanceId)) {
                continue;
            }

            LocalDateTime finSeanceExistante = s.getDateHeure().plusMinutes(s.getFilm().getDuree());

            boolean chevauchement = (dateHeure.isBefore(finSeanceExistante)
                    && finSeance.isAfter(s.getDateHeure()));

            if (chevauchement) {
                throw new RuntimeException(
                        String.format("La salle %s est déjà occupée de %s à %s (séance ID: %d)",
                                salle.getNom(), s.getDateHeure(), finSeanceExistante,
                                s.getId()));
            }
        }
    }

    private void validateNoValidatedReservations(Long seanceId) {
        Long countValidated = reservationRepository.countReservationsValideesForSeance(seanceId);
        if (countValidated != null && countValidated > 0) {
            throw new RuntimeException(
                    String.format("Impossible de modifier la séance: %d réservation(s) validée(s) existe(nt)",
                            countValidated));
        }
    }

    // ==================== HISTORIQUE ====================

    private void logHistorique(Seance seance, TypeOperation operation) {
        try {
            String email = SecurityContextHolder.getContext().getAuthentication().getName();
            Commercial commercial = commercialRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("Commercial non trouvé: " + email));

            HistoriqueSeance historique = new HistoriqueSeance();
            historique.setSeance(seance);
            historique.setCommercial(commercial);
            historique.setOperation(operation);
            historique.setDateOperation(LocalDateTime.now());

            historiqueSeanceRepository.save(historique);
        } catch (Exception e) {
            logger.error("Erreur lors de l'enregistrement de l'historique: {}", e.getMessage());
        }
    }

    public List<HistoriqueSeanceDTO> getMyHistory() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Commercial commercial = commercialRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Commercial non trouvé: " + email));

        return historiqueSeanceRepository.findByCommercialIdOrderByDateOperationDesc(commercial.getId())
                .stream()
                .map(h -> new HistoriqueSeanceDTO(
                        h.getId(),
                        h.getOperation(),
                        h.getDateOperation(),
                        h.getSeance().getId(),
                        h.getSeance().getFilm().getTitre(),
                        h.getSeance().getSalle().getNom()))
                .collect(Collectors.toList());
    }

    public SeanceResponseDTO toggleSeanceStatus(Long id) {
        Seance seance = seanceRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Séance non trouvée avec ID: " + id));

        boolean nowActif = !seance.isActif();
        seance.setActif(nowActif);
        Seance updatedSeance = seanceRepository.save(seance);

        logHistorique(updatedSeance, nowActif ? TypeOperation.ACTIVATION : TypeOperation.SUPPRESSION);

        return mapToSeanceResponseDTO(updatedSeance);
    }

    // ==================== MAPPING ====================

    private SeanceResponseDTO mapToSeanceResponseDTO(Seance seance) {
        int placesReservees = 0;
        int placesDispo = seance.getPlacesDisponibles();

        String filmTitre = "Film Inconnu";
        String filmGenre = "N/A";
        String filmId = "N/A";

        if (seance.getFilm() != null) {
            filmTitre = seance.getFilm().getTitre() != null ? seance.getFilm().getTitre() : "Film Inconnu";
            filmGenre = seance.getFilm().getGenre() != null ? seance.getFilm().getGenre() : "N/A";
            filmId = seance.getFilm().getId() != null ? seance.getFilm().getId() : "N/A";
        }

        String salleNom = "Salle Inconnue";
        String salleId = "N/A";
        int salleCapacite = 0;

        if (seance.getSalle() != null) {
            salleNom = seance.getSalle().getNom() != null ? seance.getSalle().getNom() : "Salle Inconnue";
            salleId = seance.getSalle().getId() != null ? seance.getSalle().getId() : "N/A";
            salleCapacite = seance.getSalle().getCapacite();
            placesReservees = salleCapacite - placesDispo;
        }

        String catNom = "Standard";
        Long catId = 0L;
        if (seance.getCategorie() != null) {
            catNom = seance.getCategorie().getNom() != null ? seance.getCategorie().getNom() : "Standard";
            catId = seance.getCategorie().getId() != null ? seance.getCategorie().getId() : 0L;
        }

        return new SeanceResponseDTO(
                seance.getId(),
                seance.getDateHeure(),
                seance.getPrixTicket(),
                placesReservees,
                placesDispo,
                filmTitre,
                filmGenre,
                filmId,
                salleNom,
                salleId,
                salleCapacite,
                catNom,
                catId,
                seance.getCreatedAt(),
                seance.getUpdatedAt(),
                seance.isActif());
    }

    private ReservationSimpleDTO mapToReservationSimpleDTO(Reservation reservation) {
        Long clientId = 0L;
        String clientNom = "Inconnu";
        String clientPrenom = "Inconnu";
        String clientEmail = "N/A";

        if (reservation.getClient() != null) {
            clientId = reservation.getClient().getId();
            clientNom = reservation.getClient().getNom();
            clientPrenom = reservation.getClient().getPrenom();
            clientEmail = reservation.getClient().getEmail();
        }

        Long seanceId = 0L;
        LocalDateTime seanceDate = null;
        String filmTitre = "Inconnu";

        if (reservation.getSeance() != null) {
            seanceId = reservation.getSeance().getId();
            seanceDate = reservation.getSeance().getDateHeure();
            if (reservation.getSeance().getFilm() != null) {
                filmTitre = reservation.getSeance().getFilm().getTitre();
            }
        }

        return new ReservationSimpleDTO(
                reservation.getId(),
                reservation.getNombrePlace(),
                reservation.getDateReservation(),
                reservation.getStatut(),
                reservation.getMontantTotal(),
                reservation.getTicketPdfUrl(),
                reservation.getDateValidation(),
                clientId,
                clientNom,
                clientPrenom,
                clientEmail,
                seanceId,
                seanceDate,
                filmTitre);
    }

    // ==================== STATISTIQUES DASHBOARD ====================

    @Transactional(readOnly = true)
    public DashboardStatsDTO getDashboardStats(
            java.time.LocalDate debut, java.time.LocalDate fin) {

        LocalDateTime dateDebut = debut.atStartOfDay();
        LocalDateTime dateFin = fin.atTime(23, 59, 59);

        List<Seance> seancesPeriode = seanceRepository.findByDateHeureBetween(dateDebut, dateFin);
        List<Reservation> reservationsPeriode = reservationRepository.findReservationsBetweenDates(dateDebut,
                dateFin);

        long totalSeances = seanceRepository.countByActifTrue();
        long totalReservations = reservationRepository.countByStatutNot(StatutReservation.ANNULEE);

        double revenuTotal = reservationsPeriode.stream()
                .filter(r -> r.getStatut() == StatutReservation.VALIDEE)
                .mapToDouble(r -> r.getMontantTotal() != null ? r.getMontantTotal() : 0.0)
                .sum();

        long totalCapacite = seancesPeriode.stream()
                .mapToLong(s -> s.getSalle() != null ? s.getSalle().getCapacite() : 0)
                .sum();

        long totalSiegesReserves = reservationsPeriode.stream()
                .filter(r -> r.getStatut() != StatutReservation.ANNULEE)
                .mapToLong(r -> r.getSieges() != null ? r.getSieges().size() : 0)
                .sum();

        if (totalSiegesReserves == 0 && totalReservations > 0) {
            totalSiegesReserves = reservationsPeriode.stream()
                    .filter(r -> r.getStatut() != StatutReservation.ANNULEE)
                    .mapToLong(Reservation::getNombrePlace)
                    .sum();
        }

        double tauxRemplissageGlobal = totalCapacite > 0 ? (double) totalSiegesReserves / totalCapacite * 100
                : 0.0;

        List<DailyStatsDTO> statsParJour = new java.util.ArrayList<>();
        java.time.LocalDate currentDate = debut;

        while (!currentDate.isAfter(fin)) {
            final java.time.LocalDate thisDay = currentDate;

            long dailySeancesCount = seancesPeriode.stream()
                    .filter(s -> s.getDateHeure().toLocalDate().equals(thisDay))
                    .count();

            List<Reservation> dailyRes = reservationsPeriode.stream()
                    .filter(r -> r.getDateReservation().toLocalDate().equals(thisDay))
                    .collect(Collectors.toList());

            int dailyReservationsCount = (int) dailyRes.stream()
                    .filter(r -> r.getStatut() != StatutReservation.ANNULEE)
                    .count();

            double dailyRevenu = dailyRes.stream()
                    .filter(r -> r.getStatut() == StatutReservation.VALIDEE)
                    .mapToDouble(r -> r.getMontantTotal() != null ? r.getMontantTotal() : 0.0)
                    .sum();

            statsParJour.add(new DailyStatsDTO(
                    thisDay, (int) dailySeancesCount, dailyReservationsCount, dailyRevenu));

            currentDate = currentDate.plusDays(1);
        }

        List<Seance> nextSeances = seanceRepository.findSeancesDisponibles(LocalDateTime.now()).stream()
                .limit(5)
                .collect(Collectors.toList());

        List<Reservation> lastReservations = reservationRepository.findTop5ByOrderByDateReservationDesc();

        List<SeanceResponseDTO> upcomingSeances = nextSeances.stream()
                .map(s -> {
                    try {
                        return mapToSeanceResponseDTO(s);
                    } catch (Exception e) {
                        logger.error("Error mapping seance for dashboard: {}", e.getMessage());
                        return null;
                    }
                })
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.toList());

        List<ReservationSimpleDTO> recentReservations = lastReservations.stream()
                .map(r -> {
                    try {
                        return mapToReservationSimpleDTO(r);
                    } catch (Exception e) {
                        logger.error("Error mapping reservation for dashboard: {}",
                                e.getMessage());
                        return null;
                    }
                })
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.toList());

        return new DashboardStatsDTO(
                totalSeances,
                totalReservations,
                revenuTotal,
                tauxRemplissageGlobal,
                statsParJour,
                upcomingSeances,
                recentReservations);
    }

    // ==================== DEPENDENCIES ====================

    public List<com.example.cinimana.dto.response.FilmResponseDTO> getActiveFilms() {
        return filmRepository.findAll().stream()
                .map(f -> new com.example.cinimana.dto.response.FilmResponseDTO(
                        f.getId(),
                        f.getTitre(),
                        f.getDescription(),
                        f.getDuree(),
                        f.getGenre(),
                        f.getDateSortie(),
                        f.getAfficheUrl(),
                        f.getTrailerUrl(),
                        f.getAgeLimite(),
                        f.isActif()))
                .collect(Collectors.toList());
    }

    public List<com.example.cinimana.dto.response.SalleResponseDTO> getActiveSalles() {
        return salleRepository.findAll().stream()
                .map(s -> new com.example.cinimana.dto.response.SalleResponseDTO(
                        s.getId(),
                        s.getNom(),
                        s.getCapacite(),
                        s.getType(),
                        s.getNombreRangees(),
                        s.getSiegesParRangee(),
                        s.isActif()))
                .collect(Collectors.toList());
    }

    public List<CategorieSimpleDTO> getAllCategories() {
        return categorieRepository.findAll().stream()
                .map(c -> new CategorieSimpleDTO(
                        c.getId(),
                        c.getNom(),
                        c.getDescription(),
                        c.getPrixBase()))
                .collect(Collectors.toList());
    }
}
