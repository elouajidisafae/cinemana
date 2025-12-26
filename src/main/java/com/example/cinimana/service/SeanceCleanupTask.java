package com.example.cinimana.service;

import com.example.cinimana.model.Seance;
import com.example.cinimana.repository.SeanceRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Tâche planifiée pour automatiser la désactivation des séances
 */
@Component
@RequiredArgsConstructor
public class SeanceCleanupTask {

    private static final Logger logger = LoggerFactory.getLogger(SeanceCleanupTask.class);
    private final SeanceRepository seanceRepository;

    /**
     * Désactive les séances qui ont déjà commencé.
     * S'exécute toutes les 5 minutes (300 000 ms).
     */
    @Scheduled(fixedRate = 300000) // Toutes les 5 minutes
    @Transactional
    public void deactivateExpiredSeances() {
        LocalDateTime now = LocalDateTime.now();
        logger.debug("Vérification des séances expirées à {}", now);

        List<Seance> expiredSeances = seanceRepository.findByActifTrueAndDateHeureBefore(now);

        if (!expiredSeances.isEmpty()) {
            logger.info("Désactivation de {} séances expirées", expiredSeances.size());
            for (Seance seance : expiredSeances) {
                seance.setActif(false);
            }
            seanceRepository.saveAll(expiredSeances);
        }
    }
}
