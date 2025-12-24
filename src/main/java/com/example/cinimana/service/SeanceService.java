package com.example.cinimana.service;

import com.example.cinimana.model.Seance;
import com.example.cinimana.repository.SeanceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SeanceService {

    private final SeanceRepository seanceRepository;

    public List<Seance> getAllSeances() {
        return seanceRepository.findAll();
    }

    public Seance getSeanceById(Long id) {
        return seanceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Séance non trouvée"));
    }

    /**
     * Récupère les séances disponibles pour un film donné
     * Règle métier : Seuls les séances commençant dans plus de 4 heures sont
     * disponibles
     */
    public List<Seance> getAvailableSeancesForFilm(Long filmId) {
        LocalDateTime minStartTime = LocalDateTime.now().plusHours(4);

        // Supposons une méthode findByFilmId dans le repository, sinon on filtre tout
        // Idéalement: seanceRepository.findByFilmIdAndDateHeureAfter(filmId,
        // minStartTime)

        List<Seance> allSeances = seanceRepository.findAll(); // Optimiser avec une requête personnalisée

        return allSeances.stream()
                .filter(seance -> seance.getFilm().getId().equals(filmId))
                .filter(seance -> seance.getDateHeure().isAfter(minStartTime))
                .filter(seance -> seance.getFilm().isActif()) // Vérifier si le film est actif
                .filter(Seance::isActif) // Vérifier si la séance est active
                .collect(Collectors.toList());
    }

    /**
     * Récupère toutes les séances disponibles (pour la page d'accueil ou calendrier
     * global)
     */
    public List<Seance> getAvailableSeances() {
        LocalDateTime minStartTime = LocalDateTime.now().plusHours(4);

        return seanceRepository.findAll().stream()
                .filter(seance -> seance.getDateHeure().isAfter(minStartTime))
                .filter(seance -> seance.getFilm().isActif())
                .filter(Seance::isActif) // Vérifier si la séance est active
                .collect(Collectors.toList());
    }
}
