package com.example.cinimana.repository;

import com.example.cinimana.model.HistoriqueOffre;
import com.example.cinimana.model.TypeOperation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface HistoriqueOffreRepository extends JpaRepository<HistoriqueOffre, Long> {
    List<HistoriqueOffre> findTop5ByOrderByDateOperationDesc();

    long countByOperation(TypeOperation operation);
}
