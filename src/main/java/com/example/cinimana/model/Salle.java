package com.example.cinimana.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import java.util.HashSet;
import java.util.Set;

@Entity
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class Salle extends BaseEntity {

    @Id
    @Column(length = 10, nullable = false, unique = true)
    private String id;

    @Column(unique = true, nullable = false)
    private String nom;

    @Column(nullable = false)
    private int capacite;

    private String type;

    @Column(nullable = false)
    private boolean actif = true;

    @OneToMany(mappedBy = "salle", cascade = CascadeType.ALL)
    private Set<Seance> seances = new HashSet<>();

    @OneToMany(mappedBy = "salle", cascade = CascadeType.ALL)
    private Set<HistoriqueSalle> historiques = new HashSet<>();
}
