package com.example.cinimana.repository;

import com.example.cinimana.model.Client;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface ClientRepository extends JpaRepository<Client, Long> {
    Optional<Client> findByEmail(String email);

    boolean existsByEmail(String email);

    Optional<Client> findByNumeroTelephone(String numeroTelephone);

    java.util.List<Client> findTop5ByOrderByCreatedAtDesc();
}