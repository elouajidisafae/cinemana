package com.example.cinimana.repository;

import com.example.cinimana.model.SiegeReserve;
import com.example.cinimana.model.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SiegeReserveRepository extends JpaRepository<SiegeReserve, Long> {

    List<SiegeReserve> findByReservation(Reservation reservation);

    // Trouver les sièges réservés pour une séance spécifique
    @Query("SELECT sr FROM SiegeReserve sr " +
            "JOIN sr.reservation r " +
            "WHERE r.seance.id = :seanceId " +
            "AND r.statut != 'ANNULEE'")
    List<SiegeReserve> findReservedSeatsForSeance(@Param("seanceId") Long seanceId);
    // Trouver les sièges réservés pour une séance spécifique

    // Vérifier si un siège spécifique est déjà réservé pour une séance
    @Query("SELECT COUNT(sr) > 0 FROM SiegeReserve sr " +
            "JOIN sr.reservation r " +
            "WHERE r.seance.id = :seanceId " +
            "AND sr.rangee = :rangee " +
            "AND sr.numero = :numero " +
            "AND r.statut != 'ANNULEE'")
    boolean isSeatReserved(@Param("seanceId") Long seanceId,
                           @Param("rangee") Integer rangee,
                           @Param("numero") Integer numero);
}
