package com.example.cinimana.repository;

import com.example.cinimana.model.Caissier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface CaissierRepository extends JpaRepository<Caissier, Long> {
    Optional<Caissier> findByEmail(String email);
}