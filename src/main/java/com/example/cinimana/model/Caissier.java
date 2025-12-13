package com.example.cinimana.model;

import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrimaryKeyJoinColumn;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.util.HashSet;
import java.util.Set;

@Entity
@Data
@EqualsAndHashCode(callSuper = true)
@PrimaryKeyJoinColumn(name = "id")
public class Caissier extends Utilisateur {

    @OneToMany(mappedBy = "caissier")
    private Set<Reservation> reservationsValidees = new HashSet<>();
}