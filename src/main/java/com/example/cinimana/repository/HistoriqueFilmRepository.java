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

    @Query("SELECT h FROM HistoriqueFilm h WHERE h.film.id = :filmId ORDER BY h.dateOperation DESC")
    List<HistoriqueFilm> findByFilmIdOrderByDateOperationDesc(@Param("filmId") Long filmId);

    @Query("SELECT h FROM HistoriqueFilm h WHERE h.dateOperation BETWEEN :debut AND :fin ORDER BY h.dateOperation DESC")
    List<HistoriqueFilm> findHistoriqueBetweenDates(@Param("debut") LocalDateTime debut,
                                                    @Param("fin") LocalDateTime fin);

    List<HistoriqueFilm> findTop5ByOrderByDateOperationDesc();
}