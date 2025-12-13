package com.example.cinimana.repository;

import com.example.cinimana.model.Reservation;
import com.example.cinimana.model.Client;
import com.example.cinimana.model.Seance;
import com.example.cinimana.model.StatutReservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    List<Reservation> findByClient(Client client);

    List<Reservation> findBySeance(Seance seance);

    List<Reservation> findByStatut(StatutReservation statut);

    List<Reservation> findByClientAndStatut(Client client, StatutReservation statut);

    @Query("SELECT r FROM Reservation r WHERE r.client.id = :clientId ORDER BY r.dateReservation DESC")
    List<Reservation> findByClientIdOrderByDateReservationDesc(@Param("clientId") Long clientId);

    @Query("SELECT r FROM Reservation r WHERE r.seance.id = :seanceId AND r.statut = :statut")
    List<Reservation> findBySeanceIdAndStatut(@Param("seanceId") Long seanceId,
                                              @Param("statut") StatutReservation statut);

    @Query("SELECT r FROM Reservation r WHERE r.dateReservation BETWEEN :debut AND :fin")
    List<Reservation> findReservationsBetweenDates(@Param("debut") LocalDateTime debut,
                                                   @Param("fin") LocalDateTime fin);

    @Query("SELECT COUNT(r) FROM Reservation r WHERE r.seance.id = :seanceId AND r.statut = 'VALIDEE'")
    Long countReservationsValideesForSeance(@Param("seanceId") Long seanceId);

    @Query("SELECT SUM(r.montantTotal) FROM Reservation r WHERE r.statut = 'VALIDEE' " +
            "AND r.dateReservation BETWEEN :debut AND :fin")
    Double calculateTotalRevenueBetweenDates(@Param("debut") LocalDateTime debut,
                                             @Param("fin") LocalDateTime fin);

    @Query("SELECT r FROM Reservation r WHERE r.statut = 'EN_ATTENTE' " +
            "AND r.dateReservation < :dateExpiration")
    List<Reservation> findReservationsExpirees(@Param("dateExpiration") LocalDateTime dateExpiration);

    // --- DASHBOARD ANALYTICS ---

    List<Reservation> findTop5ByOrderByDateReservationDesc();

    @Query("SELECT r.statut, COUNT(r) FROM Reservation r GROUP BY r.statut")
    List<Object[]> countReservationsByStatut();

    @Query("SELECT r.seance.film.titre, COUNT(r) FROM Reservation r GROUP BY r.seance.film.titre ORDER BY COUNT(r) DESC")
    List<Object[]> findTopFilmsByReservationCount();

    @Query("SELECT HOUR(r.dateReservation), COUNT(r) FROM Reservation r GROUP BY HOUR(r.dateReservation) ORDER BY HOUR(r.dateReservation)")
    List<Object[]> countReservationsByHour();
}