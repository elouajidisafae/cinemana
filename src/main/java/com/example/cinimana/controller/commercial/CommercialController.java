package com.example.cinimana.controller.commercial;

import com.example.cinimana.dto.commercial.request.SeanceRequestDTO;
import com.example.cinimana.dto.commercial.response.*;
import com.example.cinimana.dto.response.FilmResponseDTO;
import com.example.cinimana.dto.response.SalleResponseDTO;
import com.example.cinimana.service.commercial.CommercialSeanceService;
import com.example.cinimana.service.commercial.SeanceExcelExportService;
import com.example.cinimana.service.commercial.SeancePdfExportService;
import com.example.cinimana.service.commercial.ReservationExcelExportService;
import com.example.cinimana.service.commercial.ReservationPdfExportService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Controller REST pour les fonctionnalités Commercial
 */
@RestController
@RequestMapping("/api/commercial")
@PreAuthorize("hasRole('COMMERCIAL')")
@RequiredArgsConstructor
public class CommercialController {

    private static final Logger logger = LoggerFactory.getLogger(CommercialController.class);

    private final CommercialSeanceService seanceService;
    private final SeanceExcelExportService seanceExcelExportService;
    private final SeancePdfExportService seancePdfExportService;
    private final ReservationExcelExportService reservationExcelExportService;
    private final ReservationPdfExportService reservationPdfExportService;

    // ==================== GESTION DES SÉANCES ====================

    /**
     * Créer une nouvelle séance
     */
    @PostMapping("/seances")
    public ResponseEntity<SeanceResponseDTO> createSeance(@Valid @RequestBody SeanceRequestDTO dto) {
        logger.info("POST /api/commercial/seances - Création d'une séance");
        SeanceResponseDTO response = seanceService.createSeance(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Modifier une séance existante
     */
    @PutMapping("/seances/{id}")
    public ResponseEntity<SeanceResponseDTO> updateSeance(
            @PathVariable Long id,
            @Valid @RequestBody SeanceRequestDTO dto) {
        logger.info("PUT /api/commercial/seances/{} - Modification d'une séance", id);
        SeanceResponseDTO response = seanceService.updateSeance(id, dto);
        return ResponseEntity.ok(response);
    }

    /**
     * Lister toutes les séances avec filtres optionnels
     */
    @GetMapping("/seances")
    public ResponseEntity<List<SeanceResponseDTO>> getSeances(
            @RequestParam(required = false) String filmId,
            @RequestParam(required = false) String salleId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateDebut,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateFin) {
        logger.info("GET /api/commercial/seances - Filtres: filmId={}, salleId={}, dateDebut={}, dateFin={}",
                filmId, salleId, dateDebut, dateFin);
        List<SeanceResponseDTO> seances = seanceService.getSeances(filmId, salleId, dateDebut, dateFin);
        return ResponseEntity.ok(seances);
    }

    /**
     * Consulter une séance spécifique
     */
    @PatchMapping("/seances/{id}/status")
    public ResponseEntity<SeanceResponseDTO> toggleSeanceStatus(
            @PathVariable Long id) {
        return ResponseEntity.ok(seanceService.toggleSeanceStatus(id));
    }

    @GetMapping("/seances/{id}")
    public ResponseEntity<SeanceResponseDTO> getSeanceById(@PathVariable Long id) {
        logger.info("GET /api/commercial/seances/{} - Consultation d'une séance", id);
        SeanceResponseDTO response = seanceService.getSeanceById(id);
        return ResponseEntity.ok(response);
    }

    // ==================== CONSULTATION DES RÉSERVATIONS ====================

    /**
     * Obtenir toutes les réservations pour une séance donnée
     */
    @GetMapping("/seances/{seanceId}/reservations")
    public ResponseEntity<List<ReservationSimpleDTO>> getReservationsBySeance(
            @PathVariable Long seanceId) {
        logger.info("GET /api/commercial/seances/{}/reservations - Consultation des réservations", seanceId);
        List<ReservationSimpleDTO> reservations = seanceService.getReservationsBySeance(seanceId);
        return ResponseEntity.ok(reservations);
    }

    /**
     * Statistiques d'une séance
     */
    @GetMapping("/seances/{seanceId}/stats")
    public ResponseEntity<SeanceStatsDTO> getSeanceStats(@PathVariable Long seanceId) {
        logger.info("GET /api/commercial/seances/{}/stats - Statistiques de la séance", seanceId);
        SeanceStatsDTO stats = seanceService.getSeanceStats(seanceId);
        return ResponseEntity.ok(stats);
    }

    // ==================== DASHBOARD ====================

    /**
     * Statistiques globales pour le dashboard
     */
    @GetMapping("/dashboard/stats")
    public ResponseEntity<DashboardStatsDTO> getDashboardStats(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) java.time.LocalDate debut,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) java.time.LocalDate fin) {

        // Par défaut: 30 derniers jours
        if (fin == null)
            fin = java.time.LocalDate.now();
        if (debut == null)
            debut = fin.minusDays(29);

        logger.info("GET /api/commercial/dashboard/stats - Statistiques du {} au {}", debut, fin);
        try {
            DashboardStatsDTO stats = seanceService.getDashboardStats(debut, fin);
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            logger.error("ERREUR FATALE DASHBOARD COMMERCIAL: ", e);
            throw e; // L'exception sera rattrapée par GlobalExceptionHandler
        }
    }

    // ==================== HISTORIQUE ====================

    /**
     * Consulter l'historique des opérations du Commercial connecté
     */
    @GetMapping("/historique")
    public ResponseEntity<List<HistoriqueSeanceDTO>> getMyHistory() {
        logger.info("GET /api/commercial/historique - Consultation de l'historique");
        return ResponseEntity.ok(seanceService.getMyHistory());
    }
    // ==================== DEPENDENCIES ====================

    @GetMapping("/films")
    public ResponseEntity<List<FilmResponseDTO>> getActiveFilms() {
        return ResponseEntity.ok(seanceService.getActiveFilms());
    }

    @GetMapping("/salles")
    public ResponseEntity<List<SalleResponseDTO>> getActiveSalles() {
        return ResponseEntity.ok(seanceService.getActiveSalles());
    }

    @GetMapping("/categories")
    public ResponseEntity<List<CategorieSimpleDTO>> getAllCategories() {
        return ResponseEntity.ok(seanceService.getAllCategories());
    }

    // ==================== RESERVATIONS GLOBAL ====================

    @GetMapping("/reservations")
    public ResponseEntity<List<ReservationSimpleDTO>> getAllReservations() {
        return ResponseEntity.ok(seanceService.getAllReservations());
    }

    // ==================== EXPORTS ====================

    @GetMapping("/seances/export/excel")
    public ResponseEntity<byte[]> exportSeancesExcel(
            @RequestParam(required = false) String filmId,
            @RequestParam(required = false) String salleId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateDebut,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateFin) {

        List<SeanceResponseDTO> seances = seanceService.getSeances(filmId,
                salleId, dateDebut, dateFin);
        byte[] excelContent = seanceExcelExportService.exportSeancesToExcel(seances);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(
                MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
        headers.setContentDispositionFormData("attachment", "programme_seances.xlsx");

        return new ResponseEntity<>(excelContent, headers, HttpStatus.OK);
    }

    @GetMapping("/seances/export/pdf")
    public ResponseEntity<byte[]> exportSeancesPdf(
            @RequestParam(required = false) String filmId,
            @RequestParam(required = false) String salleId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateDebut,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateFin) {

        List<SeanceResponseDTO> seances = seanceService.getSeances(filmId,
                salleId, dateDebut, dateFin);
        byte[] pdfContent = seancePdfExportService.exportSeancesToPdf(seances);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "programme_seances.pdf");

        return new ResponseEntity<>(pdfContent, headers, HttpStatus.OK);
    }

    @GetMapping("/reservations/export/excel")
    public ResponseEntity<byte[]> exportReservationsExcel() {
        List<ReservationSimpleDTO> reservations = seanceService
                .getAllReservations();
        byte[] excelContent = reservationExcelExportService.exportReservationsToExcel(reservations);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(
                MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
        headers.setContentDispositionFormData("attachment", "rapport_reservations.xlsx");

        return new ResponseEntity<>(excelContent, headers, HttpStatus.OK);
    }

    @GetMapping("/reservations/export/pdf")
    public ResponseEntity<byte[]> exportReservationsPdf() {
        List<ReservationSimpleDTO> reservations = seanceService
                .getAllReservations();
        byte[] pdfContent = reservationPdfExportService.exportReservationsToPdf(reservations);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "rapport_reservations.pdf");

        return new ResponseEntity<>(pdfContent, headers, HttpStatus.OK);
    }
}
