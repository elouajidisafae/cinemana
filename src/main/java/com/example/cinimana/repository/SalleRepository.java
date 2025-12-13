package com.example.cinimana.repository;

import com.example.cinimana.model.Salle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface SalleRepository extends JpaRepository<Salle, String> { // <-- String ID
    Optional<Salle> findByNom(String nom);
    boolean existsByNom(String nom);

    List<Salle> findByTypeAndActifTrue(String type);
    List<Salle> findByCapaciteGreaterThanEqualAndActifTrue(int capacite);

    List<Salle> findByActifTrue();
    long countByActifTrue();
    List<Salle> findByActif(boolean actif);
    long countByActif(boolean actif);
}
