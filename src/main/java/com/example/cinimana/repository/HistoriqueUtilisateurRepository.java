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

    long countByOperation(TypeOperation operation);

    @Query("SELECT h FROM HistoriqueUtilisateur h WHERE h.utilisateur.id = :utilisateurId ORDER BY h.dateOperation DESC")
    List<HistoriqueUtilisateur> findByUtilisateurIdOrderByDateOperationDesc(
            @Param("utilisateurId") String utilisateurId);

    @Query("SELECT h FROM HistoriqueUtilisateur h WHERE h.dateOperation BETWEEN :debut AND :fin ORDER BY h.dateOperation DESC")
    List<HistoriqueUtilisateur> findHistoriqueBetweenDates(@Param("debut") LocalDateTime debut,
                                                           @Param("fin") LocalDateTime fin);

    long countByAdminIdAndOperation(Long adminId, TypeOperation operation);

    List<HistoriqueUtilisateur> findTop5ByOrderByDateOperationDesc();

    @Query("SELECT h FROM HistoriqueUtilisateur h WHERE " +
            "(:search IS NULL OR LOWER(h.admin.nom) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(h.admin.prenom) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(h.utilisateur.nom) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(h.utilisateur.prenom) LIKE LOWER(CONCAT('%', :search, '%'))) AND "
            +
            "((:operations) IS NULL OR h.operation IN (:operations)) AND " +
            "((:roles) IS NULL OR h.utilisateur.role IN (:roles)) AND " +
            "(cast(:debut as timestamp) IS NULL OR h.dateOperation >= :debut) AND " +
            "(cast(:fin as timestamp) IS NULL OR h.dateOperation <= :fin) " +
            "ORDER BY h.dateOperation DESC")
    List<HistoriqueUtilisateur> findFiltered(@Param("search") String search,
                                             @Param("operations") List<TypeOperation> operations,
                                             @Param("roles") List<Role> roles,
                                             @Param("debut") LocalDateTime debut,
                                             @Param("fin") LocalDateTime fin);
}