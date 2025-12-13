package com.example.cinimana.repository;

import com.example.cinimana.model.Commercial;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface CommercialRepository extends JpaRepository<Commercial, Long> {
    Optional<Commercial> findByEmail(String email);
}