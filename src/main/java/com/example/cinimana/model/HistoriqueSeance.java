package com.example.cinimana.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "historique_seance")
public class HistoriqueSeance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "seance_id", nullable = false)
    private Seance seance;

    @ManyToOne
    @JoinColumn(name = "commercial_id", nullable = false)
    private Commercial commercial;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TypeOperation operation;

    @Column(nullable = false)
    private LocalDateTime dateOperation;

    @PrePersist
    public void prePersist() {
        if (dateOperation == null) {
            dateOperation = LocalDateTime.now();
        }
    }
}