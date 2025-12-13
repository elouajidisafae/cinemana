package com.example.cinimana.repository;

import com.example.cinimana.model.HistoriqueUtilisateur;
import com.example.cinimana.model.Utilisateur;
import com.example.cinimana.model.Admin;
import com.example.cinimana.model.TypeOperation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;
import com.example.cinimana.model.Role;

@Repository
public interface HistoriqueUtilisateurRepository extends JpaRepository<HistoriqueUtilisateur, Long> {
    List<HistoriqueUtilisateur> findByUtilisateur(Utilisateur utilisateur);

    List<HistoriqueUtilisateur> findByAdmin(Admin admin);

    List<HistoriqueUtilisateur> findByOperation(TypeOperation operation);

    @Query("SELECT h FROM HistoriqueUtilisateur h WHERE h.utilisateur.id = :utilisateurId ORDER BY h.dateOperation DESC")
    List<HistoriqueUtilisateur> findByUtilisateurIdOrderByDateOperationDesc(
            @Param("utilisateurId") String utilisateurId);

    @Query("SELECT h FROM HistoriqueUtilisateur h WHERE h.dateOperation BETWEEN :debut AND :fin ORDER BY h.dateOperation DESC")
    List<HistoriqueUtilisateur> findHistoriqueBetweenDates(@Param("debut") LocalDateTime debut,
                                                           @Param("fin") LocalDateTime fin);

    long countByAdminIdAndOperation(Long adminId, TypeOperation operation);

    List<HistoriqueUtilisateur> findTop5ByOrderByDateOperationDesc();
}