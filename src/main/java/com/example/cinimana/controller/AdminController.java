// src/main/java/com/example/cinimana/controller/AdminController.java
package com.example.cinimana.controller;

import com.example.cinimana.dto.request.UtilisateurRequestDTO;
import com.example.cinimana.dto.response.UtilisateurResponseDTO;
import com.example.cinimana.dto.response.DashboardStatsDTO;
import com.example.cinimana.model.Role;
import com.example.cinimana.service.admin.AdminFilmService;
import com.example.cinimana.service.admin.AdminSalleService;
import com.example.cinimana.service.admin.AdminUserService;
import com.example.cinimana.service.admin.DashboardService;
import com.example.cinimana.dto.request.FilmRequestDTO;
import com.example.cinimana.dto.response.FilmResponseDTO;
import com.example.cinimana.dto.request.SalleRequestDTO;
import com.example.cinimana.dto.response.SalleResponseDTO;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.time.format.DateTimeFormatter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import com.example.cinimana.service.admin.AdminExportService;
import com.example.cinimana.dto.response.HistoriqueResponseDTO;
import com.example.cinimana.model.Client;
import com.example.cinimana.model.TypeOperation;
import com.example.cinimana.model.StatutReservation;
import java.time.LocalDateTime;
import org.springframework.format.annotation.DateTimeFormat;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminUserService adminUserService;
    private final DashboardService dashboardService;
    private final AdminFilmService adminFilmService;
    private final AdminSalleService adminSalleService;
    private final AdminExportService adminExportService;

    // --- 1. CONSULTATION DES UTILISATEURS (Actifs, Inactifs, Tous) ---
    @GetMapping("/users")
    public ResponseEntity<List<UtilisateurResponseDTO>> getAllUsers(
            @RequestParam(required = false) Boolean actif,
            @RequestParam(required = false) Role role) {

        // ✅ Assurez-vous que l'appel est bien celui-ci :
        List<UtilisateurResponseDTO> users = adminUserService.findAllUsers(actif, role);
        return ResponseEntity.ok(users);
    }

    // --- 2. AJOUT D'UN UTILISATEUR ---
    @PostMapping("/users")
    public ResponseEntity<UtilisateurResponseDTO> createUser(@Valid @RequestBody UtilisateurRequestDTO dto) {
        return new ResponseEntity<>(adminUserService.ajouterUtilisateur(dto), HttpStatus.CREATED);
    }

    // --- 3. MODIFICATION D'UN UTILISATEUR ---
    @PutMapping("/users/{id}")
    public ResponseEntity<UtilisateurResponseDTO> updateUser(@PathVariable String id,
                                                             @Valid @RequestBody UtilisateurRequestDTO dto) {
        return ResponseEntity.ok(adminUserService.modifierUtilisateur(id, dto));
    }

    // --- 4. ACTIVATION / DÉSACTIVATION / RÉACTIVATION ---
    @PutMapping("/users/{id}/activation")
    public ResponseEntity<Void> toggleUserActivation(@PathVariable String id, @RequestParam boolean actif) {
        // Si ?actif=true : Active le compte (Réactivation)
        // Si ?actif=false : Désactive le compte (Soft Delete)
        adminUserService.toggleActivation(id, actif);
        return ResponseEntity.noContent().build();
    }

    // --- 5. STATISTIQUES DU DASHBOARD ---
    @GetMapping("/dashboard/users/stats")
    public ResponseEntity<DashboardStatsDTO> getUserStats() {
        return ResponseEntity.ok(dashboardService.getUtilisateurStats());
    }

    // --- Salles ---
    @PostMapping("/salles")
    public SalleResponseDTO createSalle(@RequestBody SalleRequestDTO dto) {
        return adminSalleService.ajouterSalle(dto);
    }

    @PutMapping("/salles/{id}")
    public SalleResponseDTO updateSalle(@PathVariable String id, @RequestBody SalleRequestDTO dto) {
        return adminSalleService.modifierSalle(id, dto);
    }

    @PutMapping("/salles/{id}/activation")
    public void toggleSalle(@PathVariable String id, @RequestParam boolean actif) {
        adminSalleService.toggleActivation(id, actif);
    }

    @GetMapping("/salles")
    public List<SalleResponseDTO> getSalles(@RequestParam boolean actif) {
        return adminSalleService.findAll(actif);
    }

    // --- Films ---
    @PostMapping("/films")
    public FilmResponseDTO createFilm(@RequestBody FilmRequestDTO dto) {
        return adminFilmService.ajouterFilm(dto);
    }

    @PutMapping("/films/{id}")
    public FilmResponseDTO updateFilm(@PathVariable String id, @RequestBody FilmRequestDTO dto) {
        return adminFilmService.modifierFilm(id, dto);
    }

    @PutMapping("/films/{id}/activation")
    public void toggleFilm(@PathVariable String id, @RequestParam boolean actif) {
        adminFilmService.toggleActivation(id, actif);
    }

    @GetMapping("/films")
    public List<FilmResponseDTO> getFilms(@RequestParam boolean actif) {
        return adminFilmService.findAll(actif);
    }

    @GetMapping("/films-salles/stats")
    public ResponseEntity<DashboardService.FilmSalleStatsDTO> getFilmSalleStats() {
        return ResponseEntity.ok(dashboardService.getFilmSalleStats());
    }

    @GetMapping("/dashboard/activities")
    public ResponseEntity<List<com.example.cinimana.dto.response.HistoriqueResponseDTO>> getRecentActivities() {
        return ResponseEntity.ok(dashboardService.getRecentActivities());
    }

    @GetMapping("/dashboard/charts")
    public ResponseEntity<com.example.cinimana.dto.response.DashboardChartsDTO> getDashboardCharts() {
        return ResponseEntity.ok(dashboardService.getDashboardCharts());
    }

    // --- NOUVEAUX ENDPOINTS HISTORIQUE & MONITORING ---

    @GetMapping("/historique/users")
    public ResponseEntity<List<com.example.cinimana.dto.response.HistoriqueResponseDTO>> getFilteredUserHistory(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) List<TypeOperation> operations,
            @RequestParam(required = false) List<com.example.cinimana.model.Role> roles,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        return ResponseEntity.ok(dashboardService.getFilteredUserHistory(search, operations, roles, start, end));
    }

    @GetMapping("/historique/users/stats")
    public ResponseEntity<java.util.Map<String, Long>> getGlobalUserHistoryStats() {
        return ResponseEntity.ok(dashboardService.getGlobalUserHistoryStats());
    }

    @GetMapping("/historique/films")
    public ResponseEntity<List<com.example.cinimana.dto.response.HistoriqueResponseDTO>> getFilteredFilmHistory(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) List<TypeOperation> operations,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        return ResponseEntity.ok(dashboardService.getFilteredFilmHistory(search, operations, start, end));
    }

    @GetMapping("/historique/films/stats")
    public ResponseEntity<java.util.Map<String, Long>> getGlobalFilmHistoryStats() {
        return ResponseEntity.ok(dashboardService.getGlobalFilmHistoryStats());
    }

    @GetMapping("/historique/offres")
    public ResponseEntity<List<com.example.cinimana.dto.response.HistoriqueResponseDTO>> getFilteredOfferHistory(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) List<TypeOperation> operations,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        return ResponseEntity.ok(dashboardService.getFilteredOfferHistory(search, operations, start, end));
    }

    @GetMapping("/historique/offres/stats")
    public ResponseEntity<java.util.Map<String, Long>> getGlobalOfferHistoryStats() {
        return ResponseEntity.ok(dashboardService.getGlobalOfferHistoryStats());
    }

    @GetMapping("/historique/salles")
    public ResponseEntity<List<com.example.cinimana.dto.response.HistoriqueResponseDTO>> getFilteredSalleHistory(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) List<TypeOperation> operations,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        return ResponseEntity.ok(dashboardService.getFilteredSalleHistory(search, operations, start, end));
    }

    @GetMapping("/historique/salles/stats")
    public ResponseEntity<java.util.Map<String, Long>> getGlobalSalleHistoryStats() {
        return ResponseEntity.ok(dashboardService.getGlobalSalleHistoryStats());
    }

    @GetMapping("/historique/seances")
    public ResponseEntity<List<com.example.cinimana.dto.response.HistoriqueResponseDTO>> getFilteredSeanceHistory(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) List<TypeOperation> operations,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        return ResponseEntity.ok(dashboardService.getFilteredSeanceHistory(search, operations, start, end));
    }

    @GetMapping("/historique/seances/stats")
    public ResponseEntity<java.util.Map<String, Long>> getGlobalSeanceHistoryStats() {
        return ResponseEntity.ok(dashboardService.getGlobalSeanceHistoryStats());
    }

    @GetMapping("/historique/reservations")
    public ResponseEntity<List<com.example.cinimana.dto.response.HistoriqueResponseDTO>> getFilteredReservations(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) List<StatutReservation> statuses,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        return ResponseEntity.ok(dashboardService.getFilteredReservations(search, statuses, start, end));
    }

    @GetMapping("/historique/reservations/stats")
    public ResponseEntity<java.util.Map<String, Long>> getGlobalReservationStats() {
        return ResponseEntity.ok(dashboardService.getGlobalReservationStats());
    }

    @GetMapping("/clients")
    public ResponseEntity<List<com.example.cinimana.model.Client>> getFilteredClients(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        return ResponseEntity.ok(dashboardService.getFilteredClients(search, start, end));
    }

    // --- ENDPOINTS EXPORT EXCEL ---

    private ResponseEntity<byte[]> generateExcelResponse(byte[] content, String entityName) {
        String filename = entityName + "_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm"))
                + ".xlsx";
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .header(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, HttpHeaders.CONTENT_DISPOSITION)
                .contentType(
                        MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(content);
    }

    @GetMapping("/historique/users/export/excel")
    public ResponseEntity<byte[]> exportUserHistory(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) List<TypeOperation> operations,
            @RequestParam(required = false) List<com.example.cinimana.model.Role> roles,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        List<HistoriqueResponseDTO> data = dashboardService.getFilteredUserHistory(search, operations, roles, start,
                end);
        return generateExcelResponse(adminExportService.exportHistoriqueToExcel(data), "HistoriqueUtilisateurs");
    }

    @GetMapping("/historique/films/export/excel")
    public ResponseEntity<byte[]> exportFilmHistory(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) List<TypeOperation> operations,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        List<HistoriqueResponseDTO> data = dashboardService.getFilteredFilmHistory(search, operations, start, end);
        return generateExcelResponse(adminExportService.exportHistoriqueToExcel(data), "HistoriqueFilms");
    }

    @GetMapping("/historique/offres/export/excel")
    public ResponseEntity<byte[]> exportOfferHistory(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) List<TypeOperation> operations,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        List<HistoriqueResponseDTO> data = dashboardService.getFilteredOfferHistory(search, operations, start, end);
        return generateExcelResponse(adminExportService.exportHistoriqueToExcel(data), "HistoriqueOffres");
    }

    @GetMapping("/historique/salles/export/excel")
    public ResponseEntity<byte[]> exportSalleHistory(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) List<TypeOperation> operations,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        List<HistoriqueResponseDTO> data = dashboardService.getFilteredSalleHistory(search, operations, start, end);
        return generateExcelResponse(adminExportService.exportHistoriqueToExcel(data), "HistoriqueSalles");
    }

    @GetMapping("/historique/seances/export/excel")
    public ResponseEntity<byte[]> exportSeanceHistory(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) List<TypeOperation> operations,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        List<HistoriqueResponseDTO> data = dashboardService.getFilteredSeanceHistory(search, operations, start, end);
        return generateExcelResponse(adminExportService.exportHistoriqueToExcel(data), "HistoriqueSeances");
    }

    @GetMapping("/historique/reservations/export/excel")
    public ResponseEntity<byte[]> exportReservationHistory(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) List<StatutReservation> statuses,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        List<HistoriqueResponseDTO> data = dashboardService.getFilteredReservations(search, statuses, start, end);
        return generateExcelResponse(adminExportService.exportReservationHistoriqueToExcel(data),
                "HistoriqueReservations");
    }

    @GetMapping("/clients/export/excel")
    public ResponseEntity<byte[]> exportClients(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        List<Client> data = dashboardService.getFilteredClients(search, start, end);
        return generateExcelResponse(adminExportService.exportClientsToExcel(data), "ListeClients");
    }
}