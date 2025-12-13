package com.example.cinimana.repository;

import com.example.cinimana.model.Seance;
import com.example.cinimana.model.Film;
import com.example.cinimana.model.Salle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface SeanceRepository extends JpaRepository<Seance, Long> {
    List<Seance> findByFilm(Film film);
    List<Seance> findBySalle(Salle salle);
    List<Seance> findByDateHeureBetween(LocalDateTime debut, LocalDateTime fin);

    @Query("SELECT s FROM Seance s WHERE s.dateHeure >= :maintenant ORDER BY s.dateHeure ASC")
    List<Seance> findSeancesDisponibles(@Param("maintenant") LocalDateTime maintenant);

    @Query("SELECT s FROM Seance s WHERE s.film.id = :filmId AND s.dateHeure >= :maintenant ORDER BY s.dateHeure ASC")
    List<Seance> findSeancesByFilmIdAfterDate(@Param("filmId") Long filmId,
                                              @Param("maintenant") LocalDateTime maintenant);

    @Query("SELECT s FROM Seance s WHERE s.salle.id = :salleId AND " +
            "s.dateHeure BETWEEN :debut AND :fin")
    List<Seance> findSeancesBySalleAndDateHeureBetween(@Param("salleId") Long salleId,
                                                       @Param("debut") LocalDateTime debut,
                                                       @Param("fin") LocalDateTime fin);

    @Query("SELECT s FROM Seance s WHERE s.salle.capacite - s.placesReservees > 0 " +
            "AND s.dateHeure >= :maintenant")
    List<Seance> findSeancesAvecPlacesDisponibles(@Param("maintenant") LocalDateTime maintenant);

    @Query("SELECT SUM(r.montantTotal) FROM Seance s JOIN s.reservations r " +
            "WHERE s.dateHeure BETWEEN :debut AND :fin AND r.statut = 'VALIDEE'")
    Double calculateRevenueBetweenDates(@Param("debut") LocalDateTime debut,
                                        @Param("fin") LocalDateTime fin);
}