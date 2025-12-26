package com.example.cinimana.controller;

import com.example.cinimana.dto.response.FilmResponseDTO;
import com.example.cinimana.dto.response.SeanceResponseDTO;
import com.example.cinimana.service.client.PublicService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/public")
@RequiredArgsConstructor
@CrossOrigin(origins = { "http://localhost:5173", "http://localhost:3000",
        "http://localhost:5174" }, allowCredentials = "true")
public class PublicController {

    private final PublicService publicService;

    @GetMapping("/films")
    public ResponseEntity<List<FilmResponseDTO>> getAllActiveFilms() {
        return ResponseEntity.ok(publicService.getAllActiveFilms());
    }

    @GetMapping("/films/{id}")
    public ResponseEntity<FilmResponseDTO> getFilmDetails(@PathVariable String id) {
        return ResponseEntity.ok(publicService.getFilmDetails(id));
    }
    // RÉCUPÉRER LES SÉANCES D'UN FILM POUR LES RÉSERVATIONS
    @GetMapping("/films/{id}/seances")
    public ResponseEntity<List<SeanceResponseDTO>> getFutureSeances(@PathVariable String id) {
        return ResponseEntity.ok(publicService.getFutureSeancesByFilm(id));
    }


    // RÉCUPÉRER LES DÉTAILS D'UNE SÉANCE
    @GetMapping("/seances/{id}")
    public ResponseEntity<SeanceResponseDTO> getSeanceDetails(@PathVariable Long id) {
        return ResponseEntity.ok(publicService.getSeanceDetails(id));
    }

    @GetMapping("/seances/{id}/seats")
    public ResponseEntity<List<com.example.cinimana.model.SiegeReserve>> getReservedSeats(@PathVariable Long id) {
        // Retourne la liste des sièges occupés pour cette séance
        // Note: On pourrait retourner un DTO plus léger
        return ResponseEntity.ok(publicService.getReservedSeatsForSeance(id));
    }

    @GetMapping("/offres/applicable")
    public ResponseEntity<List<com.example.cinimana.model.Offre>> getApplicableOffers(
            @RequestParam int nbPersonnes,
            @RequestParam(required = false) String date,
            @RequestParam(required = false) Long seanceId) {

        return ResponseEntity.ok(publicService.getApplicableOffers(nbPersonnes, date, seanceId));
    }
}
