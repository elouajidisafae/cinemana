package com.example.cinimana.model;

import com.example.cinimana.config.IdGenerationListener;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;


@Entity
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Inheritance(strategy = InheritanceType.JOINED) // Use JOINED strategy for inheritance
@DiscriminatorColumn(name = "role_type", discriminatorType = DiscriminatorType.STRING)
public class Utilisateur extends BaseEntity {

    @Id
    @Column(length = 10, nullable = false, unique = true)
    private String id;

    @Column(unique = true)
    private String cin;

    private LocalDate dateNaissance;

    private LocalDate dateEmbauche;

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
    private Role role;
    @Column(nullable = false)
    private boolean premiereConnexion = true;

    @Column(nullable = false)
    private boolean actif = true;

    @OneToMany(mappedBy = "utilisateur", cascade = CascadeType.ALL)
    private Set<HistoriqueUtilisateur> historiques = new HashSet<>();


}