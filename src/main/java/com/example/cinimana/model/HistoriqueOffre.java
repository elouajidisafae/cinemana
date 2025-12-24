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
@Table(name = "historique_offre")
public class HistoriqueOffre {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "offre_id", nullable = false)
    private Offre offre;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "admin_id", nullable = false)
    private Admin admin;

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
