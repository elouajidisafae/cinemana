package com.example.cinimana.repository;

import com.example.cinimana.model.HistoriqueSalle;
import com.example.cinimana.model.Salle;
import com.example.cinimana.model.Admin;
import com.example.cinimana.model.TypeOperation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface HistoriqueSalleRepository extends JpaRepository<HistoriqueSalle, Long> {
    List<HistoriqueSalle> findBySalle(Salle salle);

    List<HistoriqueSalle> findByAdmin(Admin admin);

    List<HistoriqueSalle> findByOperation(TypeOperation operation);

    long countByOperation(TypeOperation operation);

    @Query("SELECT h FROM HistoriqueSalle h WHERE h.salle.id = :salleId ORDER BY h.dateOperation DESC")
    List<HistoriqueSalle> findBySalleIdOrderByDateOperationDesc(@Param("salleId") Long salleId);

    @Query("SELECT h FROM HistoriqueSalle h WHERE h.dateOperation BETWEEN :debut AND :fin ORDER BY h.dateOperation DESC")
    List<HistoriqueSalle> findHistoriqueBetweenDates(@Param("debut") LocalDateTime debut,
                                                     @Param("fin") LocalDateTime fin);

    List<HistoriqueSalle> findTop5ByOrderByDateOperationDesc();

    @Query("SELECT h FROM HistoriqueSalle h WHERE " +
            "(:search IS NULL OR LOWER(h.admin.nom) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(h.admin.prenom) LIKE LOWER(CONCAT('%', :search, '%'))) AND "
            +
            "((:operations) IS NULL OR h.operation IN (:operations)) AND " +
            "(cast(:debut as timestamp) IS NULL OR h.dateOperation >= :debut) AND " +
            "(cast(:fin as timestamp) IS NULL OR h.dateOperation <= :fin) " +
            "ORDER BY h.dateOperation DESC")
    List<HistoriqueSalle> findFiltered(@Param("search") String search,
                                       @Param("operations") List<TypeOperation> operations,
                                       @Param("debut") LocalDateTime debut,
                                       @Param("fin") LocalDateTime fin);
}