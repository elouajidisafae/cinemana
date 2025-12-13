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

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminUserService adminUserService;
    private final DashboardService dashboardService;
    private final AdminFilmService adminFilmService;
    private final AdminSalleService adminSalleService;

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
}