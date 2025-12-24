package com.example.cinimana.repository;

import com.example.cinimana.model.Film;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface FilmRepository extends JpaRepository<Film, String> {

    List<Film> findByGenreAndActifTrue(String genre);

    List<Film> findByTitreContainingIgnoreCaseAndActifTrue(String titre);

    List<Film> findByDateSortieBetweenAndActifTrue(LocalDate dateDebut, LocalDate dateFin);

    List<Film> findByActif(boolean actif);
    List<Film> findByActifTrue();
    long countByActifTrue();

    @Query("SELECT DISTINCT f FROM Film f JOIN f.seances s WHERE s.dateHeure >= CURRENT_TIMESTAMP AND f.actif = true")
    List<Film> findFilmsAvecSeancesDisponibles();

    boolean existsByTitre(String titre);
}
