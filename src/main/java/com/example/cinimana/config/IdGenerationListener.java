package com.example.cinimana.config;

import com.example.cinimana.model.Utilisateur;
import com.example.cinimana.model.Salle;
import com.example.cinimana.model.Film;
import com.example.cinimana.service.IdGeneratorService;
import jakarta.persistence.PrePersist;

public class IdGenerationListener {

    @PrePersist
    public void generateId(Object entity) {
        try {
            IdGeneratorService idGeneratorService = SpringContext.getBean(IdGeneratorService.class);

            if (entity instanceof Utilisateur) {
                Utilisateur user = (Utilisateur) entity;
                if (user.getId() == null || user.getId().isBlank()) {
                    user.setId(idGeneratorService.generateUniqueIdForUtilisateur());
                }
            } else if (entity instanceof Salle) {
                Salle salle = (Salle) entity;
                if (salle.getId() == null || salle.getId().isBlank()) {
                    salle.setId(idGeneratorService.generateUniqueIdForSalle());
                }
            } else if (entity instanceof Film) {
                Film film = (Film) entity;
                if (film.getId() == null || film.getId().isBlank()) {
                    film.setId(idGeneratorService.generateUniqueIdForFilm());
                }
            }

        } catch (Exception e) {
            throw new IllegalStateException("Erreur lors de la génération de l'ID", e);
        }
    }
}
