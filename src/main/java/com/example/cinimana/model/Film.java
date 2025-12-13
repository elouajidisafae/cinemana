package com.example.cinimana.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Entity
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class Film extends BaseEntity {

    @Id
    @Column(length = 10, nullable = false, unique = true)
    private String id;

    @Column(nullable = false)
    private String titre;

    @Lob
    private String description;

    @Column(nullable = false)
    private int duree;

    private String genre;

    private LocalDate dateSortie;

    private String afficheUrl;

    @Column(nullable = false)
    private boolean actif = true;

    @OneToMany(mappedBy = "film", cascade = CascadeType.ALL)
    private Set<Seance> seances = new HashSet<>();

    @OneToMany(mappedBy = "film", cascade = CascadeType.ALL)
    private Set<HistoriqueFilm> historiques = new HashSet<>();
}