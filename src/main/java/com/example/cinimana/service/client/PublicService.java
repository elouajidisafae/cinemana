package com.example.cinimana.service.client;

import com.example.cinimana.dto.response.FilmResponseDTO;
import com.example.cinimana.dto.response.SeanceResponseDTO;
import com.example.cinimana.model.Film;
import com.example.cinimana.model.Seance;
import com.example.cinimana.repository.FilmRepository;
import com.example.cinimana.repository.SeanceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PublicService {

    private final FilmRepository filmRepository;
    private final SeanceRepository seanceRepository;
    private final com.example.cinimana.repository.SiegeReserveRepository siegeReserveRepository;
    private final com.example.cinimana.service.OffreService offreService;

    private FilmResponseDTO mapFilmToDTO(Film film) {// Mapper simple Film -> FilmResponseDTO
        return new FilmResponseDTO(
                film.getId(),
                film.getTitre(),
                film.getDescription(),
                film.getDuree(),
                film.getGenre(),
                film.getDateSortie(),
                film.getAfficheUrl(),
                film.getTrailerUrl(),
                film.getAgeLimite(),
                film.isActif());
    }

    private SeanceResponseDTO mapSeanceToDTO(Seance seance) {
        try {
            // Safe mapping with defaults
            String salleNom = "Inconnue";
            int capacite = 0;
            int nombreRangees = 10;
            int siegesParRangee = 15;

            if (seance.getSalle() != null) {
                try {
                    salleNom = seance.getSalle().getNom();
                    capacite = seance.getSalle().getCapacite();
                    nombreRangees = seance.getSalle().getNombreRangees();
                    siegesParRangee = seance.getSalle().getSiegesParRangee();
                } catch (Exception e) {
                    // Ignore lazy loading errors, keep defaults
                }
            }

            String categorieNom = "Standard";
            double prix = 10.0;
            if (seance.getCategorie() != null) {
                try {
                    categorieNom = seance.getCategorie().getNom();
                    prix = seance.getCategorie().getPrixBase();
                } catch (Exception e) {
                    // Ignore lazy loading errors, keep defaults
                }
            }

            String filmId = (seance.getFilm() != null) ? seance.getFilm().getId() : null;

            return new SeanceResponseDTO(
                    seance.getId(),
                    seance.getDateHeure(),
                    prix,
                    salleNom,
                    nombreRangees,
                    siegesParRangee,
                    categorieNom,
                    capacite,
                    filmId);

        } catch (Exception e) {
            // Fallback
            return new SeanceResponseDTO(seance.getId(), seance.getDateHeure(), 0.0, "Error", 0, 0, "Error", 0, null);
        }
    }

    @Transactional(readOnly = true)
    public List<FilmResponseDTO> getAllActiveFilms() {
        return filmRepository.findByActif(true)
                .stream()// Transforme la liste des entités Film en flux (Stream) pour pouvoir les traiter facilement.
                .map(this::mapFilmToDTO)// Chaque entité Film est convertie en FilmResponseDTO.
                .collect(Collectors.toList()); // Retourner la liste des DTOs
    }

    @Transactional(readOnly = true)
    public FilmResponseDTO getFilmDetails(String id) {
        Film film = filmRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Film non trouvé"));
        return mapFilmToDTO(film);
    }

    @Transactional(readOnly = true)
    public List<SeanceResponseDTO> getFutureSeancesByFilm(String filmId) {
        return seanceRepository.findSeancesByFilmIdAfterDate(filmId, LocalDateTime.now())
                .stream()
                .map(this::mapSeanceToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public SeanceResponseDTO getSeanceDetails(Long id) {
        Seance seance = seanceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Séance non trouvée"));
        return mapSeanceToDTO(seance);
    }

    @Transactional(readOnly = true)
    public List<com.example.cinimana.model.SiegeReserve> getReservedSeatsForSeance(Long seanceId) {
        return siegeReserveRepository.findReservedSeatsForSeance(seanceId);
    }

    @Transactional(readOnly = true)
    public List<com.example.cinimana.model.Offre> getApplicableOffers(int nbPersonnes, String dateStr, Long seanceId) {
        // Check if seance is Standard category (offers only apply to Standard)
        if (seanceId != null) {
            try {
                Seance seance = seanceRepository.findById(seanceId).orElse(null);
                if (seance != null && seance.getCategorie() != null) {
                    String catNom = seance.getCategorie().getNom();
                    if (!"Standard".equalsIgnoreCase(catNom)) {
                        return java.util.Collections.emptyList(); // No offers for non-Standard
                    }
                }
            } catch (Exception e) {
                // If error checking category, don't apply offers (safer)
                return java.util.Collections.emptyList();
            }
        }

        LocalDateTime date = LocalDateTime.now();
        if (dateStr != null && !dateStr.isEmpty()) {
            try {
                // Essayer de parser la date (support pour format ISO ou simple date)
                date = LocalDateTime.parse(dateStr);
            } catch (Exception e) {
                // Fallback ou ignorer
            }
        }
        return offreService.getApplicableOffers(nbPersonnes, date);
    }
}
