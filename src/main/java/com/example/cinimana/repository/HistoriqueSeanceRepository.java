package com.example.cinimana.repository;

import com.example.cinimana.model.HistoriqueSeance;
import com.example.cinimana.model.Seance;
import com.example.cinimana.model.Commercial;
import com.example.cinimana.model.TypeOperation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface HistoriqueSeanceRepository extends JpaRepository<HistoriqueSeance, Long> {
    List<HistoriqueSeance> findBySeance(Seance seance);

    List<HistoriqueSeance> findByCommercial(Commercial commercial);

    List<HistoriqueSeance> findByOperation(TypeOperation operation);

    long countByOperation(TypeOperation operation);

    @Query("SELECT h FROM HistoriqueSeance h WHERE h.seance.id = :seanceId ORDER BY h.dateOperation DESC")
    List<HistoriqueSeance> findBySeanceIdOrderByDateOperationDesc(@Param("seanceId") Long seanceId);

    @Query("SELECT h FROM HistoriqueSeance h WHERE h.commercial.id = :commercialId ORDER BY h.dateOperation DESC")
    List<HistoriqueSeance> findByCommercialIdOrderByDateOperationDesc(@Param("commercialId") Long commercialId);

    @Query("SELECT h FROM HistoriqueSeance h WHERE h.dateOperation BETWEEN :debut AND :fin ORDER BY h.dateOperation DESC")
    List<HistoriqueSeance> findHistoriqueBetweenDates(@Param("debut") LocalDateTime debut,
                                                      @Param("fin") LocalDateTime fin);

    List<HistoriqueSeance> findTop5ByOrderByDateOperationDesc();

    @Query("SELECT h FROM HistoriqueSeance h WHERE " +
            "(:search IS NULL OR LOWER(h.commercial.nom) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(h.commercial.prenom) LIKE LOWER(CONCAT('%', :search, '%'))) AND "
            +
            "((:operations) IS NULL OR h.operation IN (:operations)) AND " +
            "(cast(:debut as timestamp) IS NULL OR h.dateOperation >= :debut) AND " +
            "(cast(:fin as timestamp) IS NULL OR h.dateOperation <= :fin) " +
            "ORDER BY h.dateOperation DESC")
    List<HistoriqueSeance> findFiltered(@Param("search") String search,
                                        @Param("operations") List<TypeOperation> operations,
                                        @Param("debut") LocalDateTime debut,
                                        @Param("fin") LocalDateTime fin);
}