package com.example.cinimana.repository;

import com.example.cinimana.model.Role;
import com.example.cinimana.model.Utilisateur;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

@Repository
public interface UtilisateurRepository extends JpaRepository<Utilisateur, String> {



    Optional<Utilisateur> findByEmail(String email);

    boolean existsByEmail(String email);

    long countByRoleAndActifTrue(Role role);

    List<Utilisateur> findByActif(boolean actif);

    long countByActifFalse();

    long countByActifTrue();
    List<Utilisateur> findByActifAndRole(Boolean actif, Role role);
    List<Utilisateur> findByRole(Role role);

    @Query("SELECT u FROM Utilisateur u WHERE " +
            "(:actif IS NULL OR u.actif = :actif) AND " +
            "(:role IS NULL OR u.role = :role)")
    List<Utilisateur> findUsersByFilters(@Param("actif") Boolean actif, @Param("role") Role role);


}