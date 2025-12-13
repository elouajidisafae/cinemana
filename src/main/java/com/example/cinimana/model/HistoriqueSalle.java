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
@Table(name = "historique_salle")
public class HistoriqueSalle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "salle_id", nullable = false)
    private Salle salle; // Référence à la salle (même si actif=false)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "admin_id", nullable = false)
    private Admin admin; // Référence à l'Admin qui a effectué l'action

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