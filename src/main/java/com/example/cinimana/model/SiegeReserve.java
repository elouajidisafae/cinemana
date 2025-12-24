package com.example.cinimana.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SiegeReserve {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "reservation_id", nullable = false)
    @com.fasterxml.jackson.annotation.JsonIgnore
    private Reservation reservation;

    @Column(nullable = false)
    private Integer rangee;

    @Column(nullable = false)
    private Integer numero;
}
