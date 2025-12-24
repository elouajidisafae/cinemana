package com.example.cinimana.repository;

import com.example.cinimana.model.Offre;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OffreRepository extends JpaRepository<Offre, Long> {
    List<Offre> findByActifTrue();

    List<Offre> findByActifFalse();

    List<Offre> findByActifTrueAndDateFinBefore(java.time.LocalDate date);
}
