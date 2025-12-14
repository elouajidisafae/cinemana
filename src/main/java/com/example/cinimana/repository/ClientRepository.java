package com.example.cinimana.repository;

import com.example.cinimana.model.Client;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ClientRepository extends JpaRepository<Client, Long> {
    Optional<Client> findByEmail(String email);

    boolean existsByEmail(String email);

    Optional<Client> findByNumeroTelephone(String numeroTelephone);

    java.util.List<Client> findTop5ByOrderByCreatedAtDesc();

    @Query("SELECT c FROM Client c WHERE " +
            "(:search IS NULL OR LOWER(c.nom) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(c.prenom) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(c.email) LIKE LOWER(CONCAT('%', :search, '%'))) AND "
            +
            "(cast(:debut as timestamp) IS NULL OR c.createdAt >= :debut) AND " +
            "(cast(:fin as timestamp) IS NULL OR c.createdAt <= :fin) " +
            "ORDER BY c.createdAt DESC")
    List<Client> searchClients(@Param("search") String search,
                               @Param("debut") LocalDateTime debut,
                               @Param("fin") LocalDateTime fin);
}