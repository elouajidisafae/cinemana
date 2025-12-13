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

    private final com.example.cinimana.repository.HistoriqueFilmRepository historiqueFilmRepository;
    private final com.example.cinimana.repository.HistoriqueSalleRepository historiqueSalleRepository;
    private final com.example.cinimana.repository.HistoriqueSeanceRepository historiqueSeanceRepository;
    private final com.example.cinimana.repository.ReservationRepository reservationRepository;

    public java.util.List<com.example.cinimana.dto.response.HistoriqueResponseDTO> getRecentActivities() {
        java.util.List<com.example.cinimana.dto.response.HistoriqueResponseDTO> activities = new java.util.ArrayList<>();

        // 1. Historique Utilisateurs
        historiqueUtilisateurRepository.findTop5ByOrderByDateOperationDesc()
                .forEach(h -> activities.add(new com.example.cinimana.dto.response.HistoriqueResponseDTO(
                        h.getId(),
                        "Utilisateur",
                        h.getUtilisateur().getNom() + " " + h.getUtilisateur().getPrenom(),
                        h.getOperation().name(),
                        h.getDateOperation(),
                        h.getAdmin().getNom() + " " + h.getAdmin().getPrenom())));

        // 2. Historique Films
        historiqueFilmRepository.findTop5ByOrderByDateOperationDesc()
                .forEach(h -> activities.add(new com.example.cinimana.dto.response.HistoriqueResponseDTO(
                        h.getId(),
                        "Film",
                        h.getFilm().getTitre(),
                        h.getOperation().name(),
                        h.getDateOperation(),
                        h.getAdmin().getNom() + " " + h.getAdmin().getPrenom())));

        // 3. Historique Salles
        historiqueSalleRepository.findTop5ByOrderByDateOperationDesc()
                .forEach(h -> activities.add(new com.example.cinimana.dto.response.HistoriqueResponseDTO(
                        h.getId(),
                        "Salle",
                        h.getSalle().getNom(),
                        h.getOperation().name(),
                        h.getDateOperation(),
                        h.getAdmin().getNom() + " " + h.getAdmin().getPrenom())));

        // 4. Historique Séances
        historiqueSeanceRepository.findTop5ByOrderByDateOperationDesc()
                .forEach(h -> activities.add(new com.example.cinimana.dto.response.HistoriqueResponseDTO(
                        h.getId(),
                        "Séance",
                        h.getSeance().getFilm().getTitre() + " (" + h.getSeance().getDateHeure() + ")",
                        h.getOperation().name(),
                        h.getDateOperation(),
                        h.getCommercial().getNom() + " " + h.getCommercial().getPrenom())));

        // 5. Historique Réservations
        reservationRepository.findTop5ByOrderByDateReservationDesc()
                .forEach(r -> activities.add(new com.example.cinimana.dto.response.HistoriqueResponseDTO(
                        r.getId(),
                        "Réservation",
                        r.getSeance().getFilm().getTitre() + " - " + r.getNombrePlace() + " places",
                        r.getStatut().name(),
                        r.getDateReservation(),
                        r.getClient().getNom() + " " + r.getClient().getPrenom())));

        // 6. Historique Clients (Nouveaux Inscrits)
        clientRepository.findTop5ByOrderByCreatedAtDesc()
                .forEach(c -> activities.add(new com.example.cinimana.dto.response.HistoriqueResponseDTO(
                        c.getId(),
                        "Client",
                        c.getNom() + " " + c.getPrenom(),
                        "INSCRIPTION",
                        c.getCreatedAt(),
                        "Client Lui-même"))); // Ou "Système"

        // Trier par date décroissante et garder les 10 premiers
        return activities.stream()
                .sorted(java.util.Comparator.comparing(
                        com.example.cinimana.dto.response.HistoriqueResponseDTO::dateOperation).reversed())
                .limit(10)
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
            topFilms.add(new com.example.cinimana.dto.response.DashboardChartsDTO.TopFilmDTO((String) obj[0],
                    (Long) obj[1]));
        });

        // 3. Heures de pointe
        java.util.Map<Integer, Long> peakHours = new java.util.TreeMap<>();
        reservationRepository.countReservationsByHour().forEach(obj -> {
            peakHours.put((Integer) obj[0], (Long) obj[1]);
        });

        return new com.example.cinimana.dto.response.DashboardChartsDTO(statusStats, topFilms, peakHours);
    }
}