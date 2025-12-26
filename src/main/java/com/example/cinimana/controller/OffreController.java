package com.example.cinimana.controller;

import com.example.cinimana.model.Offre;
import com.example.cinimana.service.OffreService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/offres")
@CrossOrigin(origins = "http://localhost:5173")
public class OffreController {

    @Autowired
    private OffreService offreService;


    @GetMapping("/public")
    public List<Offre> getActiveOffres() {
        return offreService.getActiveOffres();
    }

    // Admin: Get all offers
    @GetMapping
    public List<Offre> getAllOffres() {
        return offreService.getAllOffres();
    }

    @GetMapping("/inactive")
    public List<Offre> getInactiveOffres() {
        return offreService.getInactiveOffres();
    }

    // Admin: Get by ID
    @GetMapping("/{id}")
    public ResponseEntity<Offre> getOffreById(@PathVariable Long id) {
        return ResponseEntity.ok(offreService.getOffreById(id));
    }

    // Admin: Create
    @PostMapping
    public Offre createOffre(@RequestBody Offre offre) {
        return offreService.createOffre(offre);
    }

    // Admin: Update
    @PutMapping("/{id}")
    public ResponseEntity<Offre> updateOffre(@PathVariable Long id, @RequestBody Offre offreDetails) {
        return ResponseEntity.ok(offreService.updateOffre(id, offreDetails));
    }

    // Admin: Delete
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOffre(@PathVariable Long id) {
        offreService.deleteOffre(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/activate")
    public ResponseEntity<Void> activateOffre(@PathVariable Long id) {
        offreService.activateOffre(id);
        return ResponseEntity.ok().build();
    }
}
