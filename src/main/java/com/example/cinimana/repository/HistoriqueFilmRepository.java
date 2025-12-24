package com.example.cinimana.repository;

import com.example.cinimana.model.HistoriqueFilm;
import com.example.cinimana.model.Film;
import com.example.cinimana.model.Admin;
import com.example.cinimana.model.TypeOperation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface HistoriqueFilmRepository extends JpaRepository<HistoriqueFilm, Long> {
    List<HistoriqueFilm> findByFilm(Film film);

    List<HistoriqueFilm> findByAdmin(Admin admin);

    List<HistoriqueFilm> findByOperation(TypeOperation operation);

    long countByOperation(TypeOperation operation);

    @Query("SELECT h FROM HistoriqueFilm h WHERE h.film.id = :filmId ORDER BY h.dateOperation DESC")
    List<HistoriqueFilm> findByFilmIdOrderByDateOperationDesc(@Param("filmId") Long filmId);

    @Query("SELECT h FROM HistoriqueFilm h WHERE h.dateOperation BETWEEN :debut AND :fin ORDER BY h.dateOperation DESC")
    List<HistoriqueFilm> findHistoriqueBetweenDates(@Param("debut") LocalDateTime debut,
                                                    @Param("fin") LocalDateTime fin);

    List<HistoriqueFilm> findTop5ByOrderByDateOperationDesc();

    @Query("SELECT h FROM HistoriqueFilm h WHERE " +
            "(:search IS NULL OR LOWER(h.admin.nom) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(h.admin.prenom) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(h.film.titre) LIKE LOWER(CONCAT('%', :search, '%'))) AND "
            +
            "((:operations) IS NULL OR h.operation IN (:operations)) AND " +
            "(cast(:debut as timestamp) IS NULL OR h.dateOperation >= :debut) AND " +
            "(cast(:fin as timestamp) IS NULL OR h.dateOperation <= :fin) " +
            "ORDER BY h.dateOperation DESC")
    List<HistoriqueFilm> findFiltered(@Param("search") String search,
                                      @Param("operations") List<TypeOperation> operations,
                                      @Param("debut") LocalDateTime debut,
                                      @Param("fin") LocalDateTime fin);
}