package com.example.cinimana.model;

import jakarta.persistence.*;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import java.time.LocalDateTime;
/**
 * Base entity  pour toutes les entités de l'application.
 * Elle fournit des champs communs à toutes les entités.
 * Elle est utilisée pour suivre les dates de création et de mise à jour de toutes les entités.
 */
@Data
@MappedSuperclass // Indique que cette classe est une superclasse mappée
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseEntity {

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;
}