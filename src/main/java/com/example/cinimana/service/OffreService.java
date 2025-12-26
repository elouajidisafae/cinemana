package com.example.cinimana.service;

import com.example.cinimana.model.Offre;
import com.example.cinimana.repository.OffreRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OffreService {

    @Autowired
    private com.example.cinimana.repository.HistoriqueOffreRepository historiqueOffreRepository;

    @Autowired
    private OffreRepository offreRepository;

    @Autowired
    private UserService userService;

    private void logHistory(Offre offre, com.example.cinimana.model.TypeOperation operation) {
        try {
            com.example.cinimana.model.Admin currentAdmin = userService.getCurrentAdmin();
            com.example.cinimana.model.HistoriqueOffre history = new com.example.cinimana.model.HistoriqueOffre();
            history.setOffre(offre);
            history.setAdmin(currentAdmin);
            history.setOperation(operation);
            history.setDateOperation(java.time.LocalDateTime.now());
            historiqueOffreRepository.save(history);
        } catch (Exception e) {
            // Log error but don't fail the main operation if history logging fails
            System.err.println("Failed to log offer history: " + e.getMessage());
        }
    }

    public List<Offre> getAllOffres() {
        return offreRepository.findByActifTrue();
    }

    public List<Offre> getActiveOffres() {
        return offreRepository.findByActifTrue();
    }

    public List<Offre> getInactiveOffres() {
        return offreRepository.findByActifFalse();
    }

    public Offre getOffreById(Long id) {
        return offreRepository.findById(id).orElseThrow(() -> new RuntimeException("Offre not found"));
    }

    public Offre createOffre(Offre offre) {
        Offre savedOffre = offreRepository.save(offre);
        logHistory(savedOffre, com.example.cinimana.model.TypeOperation.CREATION);
        return savedOffre;
    }

    public Offre updateOffre(Long id, Offre offreDetails) {
        Offre offre = getOffreById(id);
        offre.setTitre(offreDetails.getTitre());
        offre.setDescription(offreDetails.getDescription());
        offre.setPrix(offreDetails.getPrix());
        offre.setDateDebut(offreDetails.getDateDebut());
        offre.setDateFin(offreDetails.getDateFin());
        offre.setActif(offreDetails.isActif());
        Offre updatedOffre = offreRepository.save(offre);
        logHistory(updatedOffre, com.example.cinimana.model.TypeOperation.MODIFICATION);
        return updatedOffre;
    }

    public void deleteOffre(Long id) {
        Offre offre = getOffreById(id);
        offre.setActif(false);
        offreRepository.save(offre);
        logHistory(offre, com.example.cinimana.model.TypeOperation.SUPPRESSION);
    }

    public void activateOffre(Long id) {
        Offre offre = getOffreById(id);
        offre.setActif(true);
        offreRepository.save(offre);
        logHistory(offre, com.example.cinimana.model.TypeOperation.ACTIVATION);
    }
//
    /**
     * Retourne les offres applicables selon le contexte
     *
     * @param nbPersonnes Nombre de personnes
     * @param date        Date de la séance (pour vérifier le jour de la semaine)
     * @return Liste des offres applicables
     */
    public List<Offre> getApplicableOffers(int nbPersonnes, java.time.LocalDateTime date) {
        List<Offre> activeOffres = offreRepository.findByActifTrue();

        // Filtrage en mémoire pour l'instant (peut être déplacé en DB si complexe)
        return activeOffres.stream()
                .filter(offre -> isOffreApplicable(offre, nbPersonnes, date))
                .collect(java.util.stream.Collectors.toList());
    }

    private boolean isOffreApplicable(Offre offre, int nbPersonnes, java.time.LocalDateTime date) {
        // Logique spécifique par titre/description (idéalement utiliserait un type
        // d'offre ou tags)
        String titreLower = offre.getTitre().toLowerCase();

        // Offre Famille : 4 personnes
        if (titreLower.contains("famille") && nbPersonnes == 4) {
            return true;
        }

        // Offre Duo : 2 personnes
        if (titreLower.contains("duo") && nbPersonnes == 2) {
            return true;
        }

        // Offre Étudiant : 1 personne (any day)
        if (titreLower.contains("étudiant") || titreLower.contains("etudiant")) {
            return nbPersonnes == 1;
        }

        // Par défaut, retourner true si pas de condition spécifique (ou false selon
        // métier)
        // Ici on retourne true pour les offres génériques sans conditions strictes
        // détectées
        return !titreLower.contains("famille") && !titreLower.contains("duo") && !titreLower.contains("étudiant");
    }
}
