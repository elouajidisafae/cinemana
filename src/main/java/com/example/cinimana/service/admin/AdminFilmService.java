package com.example.cinimana.service.admin;

import com.example.cinimana.dto.request.FilmRequestDTO;
import com.example.cinimana.dto.response.FilmResponseDTO;
import com.example.cinimana.model.*;
import com.example.cinimana.repository.FilmRepository;
import com.example.cinimana.repository.HistoriqueFilmRepository;
import com.example.cinimana.service.IdGeneratorService;
import com.example.cinimana.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminFilmService {

    private final FilmRepository filmRepository;
    private final HistoriqueFilmRepository historiqueFilmRepository;
    private final UserService userService;
    private final IdGeneratorService idGeneratorService;

    private FilmResponseDTO mapToDTO(Film film) {
        return new FilmResponseDTO(
                film.getId(),
                film.getTitre(),
                film.getDescription(),
                film.getDuree(),
                film.getGenre(),
                film.getDateSortie(),
                film.getAfficheUrl(),
                film.getTrailerUrl(),
                film.isActif());
    }

    @Transactional
    public FilmResponseDTO ajouterFilm(FilmRequestDTO dto) {
        if (filmRepository.existsByTitre(dto.titre())) {
            throw new RuntimeException("Film déjà existant");
        }

        Film film = new Film();
        film.setId(idGeneratorService.generateUniqueIdForFilm());
        film.setTitre(dto.titre());
        film.setDescription(dto.description());
        film.setDuree(dto.duree());
        film.setGenre(dto.genre());
        film.setDateSortie(dto.dateSortie());
        film.setAfficheUrl(dto.afficheUrl());
        film.setTrailerUrl(dto.trailerUrl());
        film.setActif(true);

        filmRepository.save(film);

        HistoriqueFilm h = new HistoriqueFilm();
        h.setFilm(film);
        h.setAdmin(userService.getCurrentAdmin());
        h.setOperation(TypeOperation.CREATION);
        historiqueFilmRepository.save(h);

        return mapToDTO(film);
    }

    @Transactional
    public FilmResponseDTO modifierFilm(String id, FilmRequestDTO dto) {
        Film film = filmRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Film non trouvé"));
        film.setTitre(dto.titre());
        film.setDescription(dto.description());
        film.setDuree(dto.duree());
        film.setGenre(dto.genre());
        film.setDateSortie(dto.dateSortie());
        film.setAfficheUrl(dto.afficheUrl());
        film.setTrailerUrl(dto.trailerUrl());

        filmRepository.save(film);

        HistoriqueFilm h = new HistoriqueFilm();
        h.setFilm(film);
        h.setAdmin(userService.getCurrentAdmin());
        h.setOperation(TypeOperation.MODIFICATION);
        historiqueFilmRepository.save(h);

        return mapToDTO(film);
    }

    @Transactional
    public void toggleActivation(String id, boolean actif) {
        Film film = filmRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Film non trouvé"));
        film.setActif(actif);
        filmRepository.save(film);

        HistoriqueFilm h = new HistoriqueFilm();
        h.setFilm(film);
        h.setAdmin(userService.getCurrentAdmin());
        h.setOperation(TypeOperation.ACTIVATION);
        historiqueFilmRepository.save(h);
    }

    @Transactional(readOnly = true)
    public List<FilmResponseDTO> findAll(boolean actif) {
        return filmRepository.findByActif(actif)
                .stream().map(this::mapToDTO).collect(Collectors.toList());
    }
}
