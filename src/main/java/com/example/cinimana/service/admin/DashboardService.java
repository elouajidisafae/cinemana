// src/main/java/com/example/cinimana/service/admin/DashboardService.java
package com.example.cinimana.service.admin;

import com.example.cinimana.dto.response.DashboardStatsDTO;
import com.example.cinimana.model.Role; // Import de l'enum Role
import com.example.cinimana.model.TypeOperation; // Import de l'enum TypeOperation
import com.example.cinimana.repository.ClientRepository;
import com.example.cinimana.repository.UtilisateurRepository;
import com.example.cinimana.repository.SalleRepository; // Import du repository de salle
import com.example.cinimana.repository.HistoriqueUtilisateurRepository; // Import du repository d'historique
import com.example.cinimana.service.UserService; // Import du service pour l'Admin courant
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final UtilisateurRepository utilisateurRepository;
    private final UserService userService;
    private final ClientRepository clientRepository; // ✅ NOUVELLE INJECTION
    private final HistoriqueUtilisateurRepository historiqueUtilisateurRepository;
    private final SalleRepository salleRepository;
    private final com.example.cinimana.repository.FilmRepository filmRepository;

    // ✅ Repository nécessaire
    // 1. Statistiques des utilisateurs
    public DashboardStatsDTO getUtilisateurStats() {
        long totalActifs = utilisateurRepository.countByActifTrue();
        long totalInactifs = utilisateurRepository.countByActifFalse();

        // Récupérer l'ID de l'Admin courant
        Long currentAdminId = userService.getCurrentAdmin().getId();

        // ✅ Calcul de la nouvelle statistique
        long totalAjouteParAdmin = historiqueUtilisateurRepository.countByAdminIdAndOperation(
                currentAdminId,
                TypeOperation.CREATION);
        long totalClientsCrees = clientRepository.count();

        return new DashboardStatsDTO(
                totalActifs,
                totalInactifs,
                utilisateurRepository.countByRoleAndActifTrue(Role.COMMERCIAL),
                utilisateurRepository.countByRoleAndActifTrue(Role.CAISSIER),
                utilisateurRepository.count(),
                totalAjouteParAdmin,
                totalClientsCrees);
    }

    public record FilmSalleStatsDTO(long totalSallesActives, long totalSallesInactives,
                                    long totalFilmsActifs, long totalFilmsInactifs) {
    }

    public FilmSalleStatsDTO getFilmSalleStats() {
        long sallesActives = salleRepository.countByActifTrue();
        long sallesInactives = salleRepository.countByActif(false);
        long filmsActifs = filmRepository.countByActifTrue();
        long filmsInactifs = filmRepository.findByActif(false).size();
        return new FilmSalleStatsDTO(sallesActives, sallesInactives, filmsActifs, filmsInactifs);
    }

    private final com.example.cinimana.repository.HistoriqueOffreRepository historiqueOffreRepository;
    private final com.example.cinimana.repository.HistoriqueFilmRepository historiqueFilmRepository;
    private final com.example.cinimana.repository.HistoriqueSalleRepository historiqueSalleRepository;
    private final com.example.cinimana.repository.HistoriqueSeanceRepository historiqueSeanceRepository;
    private final com.example.cinimana.repository.ReservationRepository reservationRepository;

    public java.util.List<com.example.cinimana.dto.response.HistoriqueResponseDTO> getRecentActivities() {
        java.util.List<com.example.cinimana.dto.response.HistoriqueResponseDTO> activities = new java.util.ArrayList<>();

        // 1. Historique Utilisateurs
        historiqueUtilisateurRepository.findTop5ByOrderByDateOperationDesc()
                .forEach(h -> activities
                        .add(new com.example.cinimana.dto.response.HistoriqueResponseDTO(
                                h.getId(),
                                "Utilisateur",
                                h.getUtilisateur().getId(),
                                h.getUtilisateur().getNom() + " "
                                        + h.getUtilisateur().getPrenom(),
                                h.getOperation().name(),
                                h.getDateOperation(),
                                h.getAdmin().getNom() + " "
                                        + h.getAdmin().getPrenom(),
                                null, null)));

        // 2. Historique Films
        historiqueFilmRepository.findTop5ByOrderByDateOperationDesc()
                .forEach(h -> activities
                        .add(new com.example.cinimana.dto.response.HistoriqueResponseDTO(
                                h.getId(),
                                "Film",
                                String.valueOf(h.getFilm().getId()),
                                h.getFilm().getTitre(),
                                h.getOperation().name(),
                                h.getDateOperation(),
                                h.getAdmin().getNom() + " "
                                        + h.getAdmin().getPrenom(),
                                null, null)));

        // 3. Historique Salles
        historiqueSalleRepository.findTop5ByOrderByDateOperationDesc()
                .forEach(h -> activities
                        .add(new com.example.cinimana.dto.response.HistoriqueResponseDTO(
                                h.getId(),
                                "Salle",
                                String.valueOf(h.getSalle().getId()),
                                h.getSalle().getNom(),
                                h.getOperation().name(),
                                h.getDateOperation(),
                                h.getAdmin().getNom() + " "
                                        + h.getAdmin().getPrenom(),
                                null, null)));

        // 4. Historique Séances
        historiqueSeanceRepository.findTop5ByOrderByDateOperationDesc()
                .forEach(h -> activities
                        .add(new com.example.cinimana.dto.response.HistoriqueResponseDTO(
                                h.getId(),
                                "Séance",
                                String.valueOf(h.getSeance().getId()),
                                h.getSeance().getFilm().getTitre() + " ("
                                        + h.getSeance().getDateHeure() + ")",
                                h.getOperation().name(),
                                h.getDateOperation(),
                                h.getCommercial().getNom() + " "
                                        + h.getCommercial().getPrenom(),
                                null, h.getSeance().getDateHeure().toString())));

        // 5. Historique Réservations
        reservationRepository.findTop5ByOrderByDateReservationDesc()
                .forEach(r -> activities
                        .add(new com.example.cinimana.dto.response.HistoriqueResponseDTO(
                                r.getId(),
                                "Réservation",
                                String.valueOf(r.getId()),
                                r.getSeance().getFilm().getTitre() + " - "
                                        + r.getNombrePlace() + " places",
                                r.getStatut().name(),
                                r.getDateReservation(),
                                r.getClient().getNom() + " "
                                        + r.getClient().getPrenom(),
                                r.getMontantTotal(),
                                r.getSeance().getDateHeure().toString())));

        // 6. Historique Clients (Nouveaux Inscrits)
        clientRepository.findTop5ByOrderByCreatedAtDesc()
                .forEach(c -> activities
                        .add(new com.example.cinimana.dto.response.HistoriqueResponseDTO(
                                c.getId(),
                                "Client",
                                String.valueOf(c.getId()),
                                c.getNom() + " " + c.getPrenom(),
                                "INSCRIPTION",
                                c.getCreatedAt(),
                                "Client Lui-même", null, null)));

        // 7. Historique Offres
        historiqueOffreRepository.findTop5ByOrderByDateOperationDesc()
                .forEach(h -> activities
                        .add(new com.example.cinimana.dto.response.HistoriqueResponseDTO(
                                h.getId(),
                                "Offre",
                                String.valueOf(h.getOffre().getId()),
                                h.getOffre().getTitre(),
                                h.getOperation().name(),
                                h.getDateOperation(),
                                h.getAdmin().getNom() + " "
                                        + h.getAdmin().getPrenom(),
                                null, null)));

        // Grouper par type et garder uniquement la plus récente pour chaque type
        return activities.stream()
                .collect(java.util.stream.Collectors.groupingBy(
                        com.example.cinimana.dto.response.HistoriqueResponseDTO::entiteType,
                        java.util.stream.Collectors.collectingAndThen(
                                java.util.stream.Collectors
                                        .maxBy(java.util.Comparator.comparing(
                                                com.example.cinimana.dto.response.HistoriqueResponseDTO::dateOperation)),
                                java.util.Optional::get)))
                .values().stream()
                .sorted(java.util.Comparator.comparing(
                                com.example.cinimana.dto.response.HistoriqueResponseDTO::dateOperation)
                        .reversed())
                .collect(java.util.stream.Collectors.toList());
    }

    public com.example.cinimana.dto.response.DashboardChartsDTO getDashboardCharts() {
        // 1. Statut des réservations
        java.util.Map<String, Long> statusStats = new java.util.HashMap<>();
        reservationRepository.countReservationsByStatut().forEach(obj -> {
            statusStats.put(((com.example.cinimana.model.StatutReservation) obj[0]).name(), (Long) obj[1]);
        });

        // 2. Top 5 Films
        java.util.List<com.example.cinimana.dto.response.DashboardChartsDTO.TopFilmDTO> topFilms = new java.util.ArrayList<>();
        reservationRepository.findTopFilmsByReservationCount().stream().limit(5).forEach(obj -> {
            topFilms.add(new com.example.cinimana.dto.response.DashboardChartsDTO.TopFilmDTO(
                    (String) obj[0],
                    (Long) obj[1]));
        });

        // 3. Heures de pointe
        java.util.Map<Integer, Long> peakHours = new java.util.TreeMap<>();
        reservationRepository.countReservationsByHour().forEach(obj -> {
            peakHours.put((Integer) obj[0], (Long) obj[1]);
        });

        // 4. Revenus des 7 derniers jours
        java.util.Map<String, Double> dailyRevenue = new java.util.LinkedHashMap<>();
        java.time.LocalDateTime sevenDaysAgo = java.time.LocalDateTime.now().minusDays(7).withHour(0)
                .withMinute(0);
        reservationRepository.getDailyRevenue(sevenDaysAgo).forEach(obj -> {
            dailyRevenue.put(obj[0].toString(), (Double) obj[1]);
        });

        // 5. Répartition par genre
        java.util.Map<String, Long> genreDistribution = new java.util.HashMap<>();
        reservationRepository.countReservationsByGenre().forEach(obj -> {
            genreDistribution.put((String) obj[0], (Long) obj[1]);
        });

        // 6. Tendances des statuts (7 jours)
        java.util.Map<String, java.util.Map<String, Long>> dailyStatusStats = new java.util.LinkedHashMap<>();
        reservationRepository.getDailyStatusStats(sevenDaysAgo).forEach(obj -> {
            String dateStr = obj[0].toString();
            String statusStr = ((com.example.cinimana.model.StatutReservation) obj[1]).name();
            Long count = (Long) obj[2];

            dailyStatusStats.computeIfAbsent(dateStr, k -> new java.util.HashMap<>()).put(statusStr, count);
        });

        return new com.example.cinimana.dto.response.DashboardChartsDTO(statusStats, topFilms, peakHours,
                dailyRevenue, genreDistribution, dailyStatusStats);
    }

    // --- HISTORIQUE FILTRÉ ---

    public java.util.List<com.example.cinimana.dto.response.HistoriqueResponseDTO> getFilteredUserHistory(
            String search, java.util.List<TypeOperation> operations,
            java.util.List<com.example.cinimana.model.Role> roles,
            java.time.LocalDateTime start,
            java.time.LocalDateTime end) {
        return historiqueUtilisateurRepository.findFiltered(search, operations, roles, start, end).stream()
                .map(h -> new com.example.cinimana.dto.response.HistoriqueResponseDTO(
                        h.getId(), h.getUtilisateur().getRole().name(), // Mapping to Role
                        h.getUtilisateur().getId(),
                        h.getUtilisateur().getNom() + " " + h.getUtilisateur().getPrenom(),
                        h.getOperation().name(), h.getDateOperation(),
                        h.getAdmin().getNom() + " " + h.getAdmin().getPrenom(), null, null))
                .collect(java.util.stream.Collectors.toList());
    }

    public java.util.List<com.example.cinimana.dto.response.HistoriqueResponseDTO> getFilteredOfferHistory(
            String search, java.util.List<TypeOperation> operations, java.time.LocalDateTime start,
            java.time.LocalDateTime end) {
        return historiqueOffreRepository.findAll().stream()
                .filter(h -> (search == null || h.getOffre().getTitre().toLowerCase()
                        .contains(search.toLowerCase())))
                .filter(h -> (operations == null || operations.contains(h.getOperation())))
                .filter(h -> (start == null || h.getDateOperation().isAfter(start)))
                .filter(h -> (end == null || h.getDateOperation().isBefore(end)))
                .map(h -> new com.example.cinimana.dto.response.HistoriqueResponseDTO(
                        h.getId(), "Offre", String.valueOf(h.getOffre().getId()),
                        h.getOffre().getTitre(),
                        h.getOperation().name(), h.getDateOperation(),
                        h.getAdmin().getNom() + " " + h.getAdmin().getPrenom(), null, null))
                .sorted(java.util.Comparator.comparing(
                                com.example.cinimana.dto.response.HistoriqueResponseDTO::dateOperation)
                        .reversed())
                .collect(java.util.stream.Collectors.toList());
    }

    public java.util.Map<String, Long> getGlobalOfferHistoryStats() {
        java.util.Map<String, Long> stats = new java.util.HashMap<>();
        stats.put("CREATION", historiqueOffreRepository.countByOperation(TypeOperation.CREATION));
        stats.put("MODIFICATION", historiqueOffreRepository.countByOperation(TypeOperation.MODIFICATION));
        stats.put("SUPPRESSION", historiqueOffreRepository.countByOperation(TypeOperation.SUPPRESSION));
        return stats;
    }

    public java.util.List<com.example.cinimana.dto.response.HistoriqueResponseDTO> getFilteredFilmHistory(
            String search, java.util.List<TypeOperation> operations, java.time.LocalDateTime start,
            java.time.LocalDateTime end) {
        return historiqueFilmRepository.findFiltered(search, operations, start, end).stream()
                .map(h -> new com.example.cinimana.dto.response.HistoriqueResponseDTO(
                        h.getId(), "Film", String.valueOf(h.getFilm().getId()),
                        h.getFilm().getTitre(),
                        h.getOperation().name(), h.getDateOperation(),
                        h.getAdmin().getNom() + " " + h.getAdmin().getPrenom(), null, null))
                .collect(java.util.stream.Collectors.toList());
    }

    public java.util.Map<String, Long> getGlobalFilmHistoryStats() {
        java.util.Map<String, Long> stats = new java.util.HashMap<>();
        stats.put("CREATION", historiqueFilmRepository.countByOperation(TypeOperation.CREATION));
        stats.put("MODIFICATION", historiqueFilmRepository.countByOperation(TypeOperation.MODIFICATION));
        stats.put("SUPPRESSION", historiqueFilmRepository.countByOperation(TypeOperation.SUPPRESSION));
        return stats;
    }

    public java.util.List<com.example.cinimana.dto.response.HistoriqueResponseDTO> getFilteredSalleHistory(
            String search, java.util.List<TypeOperation> operations, java.time.LocalDateTime start,
            java.time.LocalDateTime end) {
        return historiqueSalleRepository.findFiltered(search, operations, start, end).stream()
                .map(h -> new com.example.cinimana.dto.response.HistoriqueResponseDTO(
                        h.getId(), "Salle", String.valueOf(h.getSalle().getId()),
                        h.getSalle().getNom(),
                        h.getOperation().name(), h.getDateOperation(),
                        h.getAdmin().getNom() + " " + h.getAdmin().getPrenom(), null, null))
                .collect(java.util.stream.Collectors.toList());
    }

    public java.util.Map<String, Long> getGlobalSalleHistoryStats() {
        java.util.Map<String, Long> stats = new java.util.HashMap<>();
        stats.put("CREATION", historiqueSalleRepository.countByOperation(TypeOperation.CREATION));
        stats.put("MODIFICATION", historiqueSalleRepository.countByOperation(TypeOperation.MODIFICATION));
        stats.put("SUPPRESSION", historiqueSalleRepository.countByOperation(TypeOperation.SUPPRESSION));
        return stats;
    }

    public java.util.List<com.example.cinimana.dto.response.HistoriqueResponseDTO> getFilteredSeanceHistory(
            String search, java.util.List<TypeOperation> operations, java.time.LocalDateTime start,
            java.time.LocalDateTime end) {
        return historiqueSeanceRepository.findFiltered(search, operations, start, end).stream()
                .map(h -> new com.example.cinimana.dto.response.HistoriqueResponseDTO(
                        h.getId(), "Séance", String.valueOf(h.getSeance().getId()), // entiteId
                        h.getSeance().getFilm().getTitre() + " (" + h.getSeance().getDateHeure()
                                + ")",
                        h.getOperation().name(), h.getDateOperation(),
                        h.getCommercial().getNom() + " " + h.getCommercial().getPrenom(), null,
                        h.getSeance().getDateHeure().toString()))
                .collect(java.util.stream.Collectors.toList());
    }

    public java.util.Map<String, Long> getGlobalSeanceHistoryStats() {
        java.util.Map<String, Long> stats = new java.util.HashMap<>();
        stats.put("CREATION", historiqueSeanceRepository.countByOperation(TypeOperation.CREATION));
        stats.put("MODIFICATION", historiqueSeanceRepository.countByOperation(TypeOperation.MODIFICATION));
        stats.put("SUPPRESSION", historiqueSeanceRepository.countByOperation(TypeOperation.SUPPRESSION));
        return stats;
    }

    public java.util.List<com.example.cinimana.dto.response.HistoriqueResponseDTO> getFilteredReservations(
            String search, java.util.List<com.example.cinimana.model.StatutReservation> statuses,
            java.time.LocalDateTime start,
            java.time.LocalDateTime end) {
        return reservationRepository.findFiltered(search, statuses, start, end).stream()
                .map(r -> new com.example.cinimana.dto.response.HistoriqueResponseDTO(
                        r.getId(), "Réservation", String.valueOf(r.getId()),
                        r.getSeance().getFilm().getTitre() + " - " + r.getNombrePlace()
                                + " places",
                        r.getStatut().name(), r.getDateReservation(),
                        r.getClient().getNom() + " " + r.getClient().getPrenom(),
                        r.getMontantTotal(), r.getSeance().getDateHeure().toString()))
                .collect(java.util.stream.Collectors.toList());
    }

    public java.util.Map<String, Long> getGlobalReservationStats() {
        java.util.Map<String, Long> stats = new java.util.HashMap<>();
        stats.put("VALIDEE", reservationRepository
                .countByStatut(com.example.cinimana.model.StatutReservation.VALIDEE));
        stats.put("EN_ATTENTE", reservationRepository
                .countByStatut(com.example.cinimana.model.StatutReservation.EN_ATTENTE));
        stats.put("ANNULEE", reservationRepository
                .countByStatut(com.example.cinimana.model.StatutReservation.ANNULEE));
        return stats;
    }

    public java.util.List<com.example.cinimana.model.Client> getFilteredClients(
            String search, java.time.LocalDateTime start, java.time.LocalDateTime end) {
        return clientRepository.searchClients(search, start, end);
    }

    public java.util.Map<String, Long> getGlobalUserHistoryStats() {
        java.util.Map<String, Long> stats = new java.util.HashMap<>();
        stats.put("CREATION", historiqueUtilisateurRepository.countByOperation(TypeOperation.CREATION));
        stats.put("MODIFICATION", historiqueUtilisateurRepository.countByOperation(TypeOperation.MODIFICATION));
        stats.put("SUPPRESSION", historiqueUtilisateurRepository.countByOperation(TypeOperation.SUPPRESSION));
        return stats;
    }
}
