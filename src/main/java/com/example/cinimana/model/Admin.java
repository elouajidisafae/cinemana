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
public class Admin extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nom;

    @Column(nullable = false)
    private String prenom;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String motDePasse;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role = Role.ADMIN;


    @OneToMany(mappedBy = "admin", cascade = CascadeType.ALL)
    private Set<HistoriqueFilm> historiquesFilms = new HashSet<>();

    @OneToMany(mappedBy = "admin", cascade = CascadeType.ALL)
    private Set<HistoriqueSalle> historiquesSalles = new HashSet<>();

    @OneToMany(mappedBy = "admin", cascade = CascadeType.ALL)
    private Set<HistoriqueUtilisateur> historiquesUtilisateurs = new HashSet<>();
}