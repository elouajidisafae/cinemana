package com.example.cinimana.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class Seance extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDateTime dateHeure;

    @Column(nullable = false)
    private boolean actif = true;


    @ManyToOne
    @JoinColumn(name = "film_id", nullable = false)
    private Film film;

    @ManyToOne
    @JoinColumn(name = "salle_id", nullable = false)
    private Salle salle;

    @ManyToOne
    @JoinColumn(name = "categorie_id", nullable = false)
    private Categorie categorie;

    @OneToMany(mappedBy = "seance", cascade = CascadeType.ALL)
    @com.fasterxml.jackson.annotation.JsonIgnore
    private Set<Reservation> reservations = new HashSet<>();

    @OneToMany(mappedBy = "seance", cascade = CascadeType.ALL)
    @com.fasterxml.jackson.annotation.JsonIgnore
    private Set<HistoriqueSeance> historiques = new HashSet<>();

    /**
     * Récupère le prix du ticket depuis la catégorie associée
     */
    @Transient // Non persisté en base
    public double getPrixTicket() {
        try {
            return categorie != null ? categorie.getPrixBase() : 10.0;
        } catch (Exception e) {
            // Fallback in case of lazy loading exception
            return 10.0;
        }
    }

    /**
     * Calcule les places disponibles en comptant les sièges réservés
     */
    @Transient
    @com.fasterxml.jackson.annotation.JsonIgnore
    public int getPlacesDisponibles() {
        if (salle == null)
            return 0;

        if (reservations == null)
            return salle.getCapacite(); // Safety

        return salle.getCapacite() - (int) reservations.stream()
                .filter(r -> r.getStatut() != StatutReservation.ANNULEE)
                .mapToLong(r -> r.getSieges() != null ? r.getSieges().size() : 0)
                .sum();
    }
}