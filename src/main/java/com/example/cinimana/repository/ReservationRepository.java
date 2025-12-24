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

    long countByStatut(StatutReservation statut);

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

    // New Analytics for Admin
    @Query("SELECT CAST(r.dateReservation AS date), SUM(r.montantTotal) FROM Reservation r WHERE r.statut = 'VALIDEE' AND r.dateReservation >= :sevenDaysAgo GROUP BY CAST(r.dateReservation AS date) ORDER BY CAST(r.dateReservation AS date)")
    List<Object[]> getDailyRevenue(@Param("sevenDaysAgo") LocalDateTime sevenDaysAgo);

    @Query("SELECT r.seance.film.genre, COUNT(r) FROM Reservation r GROUP BY r.seance.film.genre")
    List<Object[]> countReservationsByGenre();

    @Query("SELECT CAST(r.dateReservation AS date), r.statut, COUNT(r) FROM Reservation r WHERE r.dateReservation >= :sevenDaysAgo GROUP BY CAST(r.dateReservation AS date), r.statut ORDER BY CAST(r.dateReservation AS date)")
    List<Object[]> getDailyStatusStats(@Param("sevenDaysAgo") LocalDateTime sevenDaysAgo);

    @Query("SELECT r FROM Reservation r WHERE " +
            "(:search IS NULL OR LOWER(r.client.nom) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(r.client.prenom) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(r.seance.film.titre) LIKE LOWER(CONCAT('%', :search, '%'))) AND "
            +
            "((:statuses) IS NULL OR r.statut IN (:statuses)) AND " +
            "(cast(:debut as timestamp) IS NULL OR r.dateReservation >= :debut) AND " +
            "(cast(:fin as timestamp) IS NULL OR r.dateReservation <= :fin) " +
            "ORDER BY r.dateReservation DESC")
    List<Reservation> findFiltered(@Param("search") String search,
                                   @Param("statuses") List<StatutReservation> statuses,
                                   @Param("debut") LocalDateTime debut,
                                   @Param("fin") LocalDateTime fin);

    // --- NOUVELLES QUERIES POUR SYSTÈME DE RÉSERVATION ---

    // Trouver par code de réservation (pour QR code scan)
    Reservation findByCodeReservation(String codeReservation);

    // Trouver les réservations nécessitant un email de confirmation (3h avant
    // séance)
    @Query("SELECT r FROM Reservation r WHERE " +
            "r.statut = 'EN_ATTENTE' AND " +
            "r.dateConfirmationEmail IS NULL AND " +
            "r.seance.dateHeure BETWEEN :debut AND :fin")
    List<Reservation> findReservationsNeedingConfirmationEmail(
            @Param("debut") LocalDateTime debut,
            @Param("fin") LocalDateTime fin);

    @Query("SELECT r FROM Reservation r WHERE " +
            "r.statut = 'EN_ATTENTE' AND " +
            "r.dateConfirmationEmail IS NOT NULL AND " +
            "r.dateConfirmationClient IS NULL AND " +
            "r.dateConfirmationEmail < :dateLimit")
    List<Reservation> findReservationsToCancel(@Param("dateLimit") LocalDateTime dateLimit);

    @Query("SELECT r FROM Reservation r WHERE (r.statut = 'EN_ATTENTE' OR r.statut = 'CONFIRMEE_CLIENT') AND r.seance.dateHeure < :threshold")
    List<Reservation> findNoShowReservations(@Param("threshold") LocalDateTime threshold);

    long countByStatutNot(StatutReservation statut);

    // --- CAISSIER STATISTICS ---

    long countByStatutAndDateValidationBetween(StatutReservation statut, LocalDateTime debut, LocalDateTime fin);

    @Query("SELECT SUM(r.nombrePlace) FROM Reservation r WHERE r.statut = :statut AND r.dateValidation BETWEEN :debut AND :fin")
    Integer sumNombrePlacesByStatutAndDateValidationBetween(
            @Param("statut") StatutReservation statut,
            @Param("debut") LocalDateTime debut,
            @Param("fin") LocalDateTime fin);

    @Query("SELECT SUM(r.montantTotal) FROM Reservation r WHERE r.statut = :statut AND r.dateValidation BETWEEN :debut AND :fin")
    Double sumMontantByStatutAndDateValidationBetween(
            @Param("statut") StatutReservation statut,
            @Param("debut") LocalDateTime debut,
            @Param("fin") LocalDateTime fin);

    List<Reservation> findTop5ByStatutOrderByDateValidationDesc(StatutReservation statut);



    // --- CAISSIER SPECIFIC STATS ---

    long countByStatutAndCaissierAndDateValidationBetween(StatutReservation statut,
                                                          com.example.cinimana.model.Caissier caissier, LocalDateTime debut, LocalDateTime fin);

    @Query("SELECT SUM(r.nombrePlace) FROM Reservation r WHERE r.statut = :statut AND r.caissier = :caissier AND r.dateValidation BETWEEN :debut AND :fin")
    Integer sumNombrePlacesByStatutAndCaissierAndDateValidationBetween(
            @Param("statut") StatutReservation statut,
            @Param("caissier") com.example.cinimana.model.Caissier caissier,
            @Param("debut") LocalDateTime debut,
            @Param("fin") LocalDateTime fin);

    @Query("SELECT SUM(r.montantTotal) FROM Reservation r WHERE r.statut = :statut AND r.caissier = :caissier AND r.dateValidation BETWEEN :debut AND :fin")
    Double sumMontantByStatutAndCaissierAndDateValidationBetween(
            @Param("statut") StatutReservation statut,
            @Param("caissier") com.example.cinimana.model.Caissier caissier,
            @Param("debut") LocalDateTime debut,
            @Param("fin") LocalDateTime fin);

    List<Reservation> findTop5ByStatutAndCaissierOrderByDateValidationDesc(StatutReservation statut,
                                                                           com.example.cinimana.model.Caissier caissier);
}
