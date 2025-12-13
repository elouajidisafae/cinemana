package com.example.cinimana.model;

import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrimaryKeyJoinColumn;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.util.HashSet;
import java.util.Set;
import jakarta.persistence.CascadeType;

@Entity
@Data
@EqualsAndHashCode(callSuper = true)
@PrimaryKeyJoinColumn(name = "id")
public class Commercial extends Utilisateur {

    @OneToMany(mappedBy = "commercial", cascade = CascadeType.ALL)
    private Set<HistoriqueSeance> historiquesSeances = new HashSet<>();
}