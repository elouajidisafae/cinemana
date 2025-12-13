package com.example.cinimana.service.admin;

import com.example.cinimana.dto.request.SalleRequestDTO;
import com.example.cinimana.dto.response.SalleResponseDTO;
import com.example.cinimana.model.*;
import com.example.cinimana.repository.HistoriqueSalleRepository;
import com.example.cinimana.repository.SalleRepository;
import com.example.cinimana.service.IdGeneratorService;
import com.example.cinimana.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminSalleService {

    private final SalleRepository salleRepository;
    private final HistoriqueSalleRepository historiqueSalleRepository;
    private final UserService userService;
    private final IdGeneratorService idGeneratorService;

    private SalleResponseDTO mapToDTO(Salle salle) {
        return new SalleResponseDTO(
                salle.getId(),
                salle.getNom(),
                salle.getCapacite(),
                salle.getType(),
                salle.isActif()
        );
    }

    @Transactional
    public SalleResponseDTO ajouterSalle(SalleRequestDTO dto) {
        if (salleRepository.existsByNom(dto.nom())) {
            throw new RuntimeException("Le nom de salle existe déjà");
        }

        Salle salle = new Salle();
        salle.setId(idGeneratorService.generateUniqueIdForSalle());
        salle.setNom(dto.nom());
        salle.setCapacite(dto.capacite());
        salle.setType(dto.type());
        salle.setActif(true);

        salleRepository.save(salle);

        // Historique
        HistoriqueSalle h = new HistoriqueSalle();
        h.setSalle(salle);
        h.setAdmin(userService.getCurrentAdmin());
        h.setOperation(TypeOperation.CREATION);
        historiqueSalleRepository.save(h);

        return mapToDTO(salle);
    }

    @Transactional
    public SalleResponseDTO modifierSalle(String id, SalleRequestDTO dto) {
        Salle salle = salleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Salle non trouvée"));
        salle.setNom(dto.nom());
        salle.setCapacite(dto.capacite());
        salle.setType(dto.type());

        salleRepository.save(salle);

        HistoriqueSalle h = new HistoriqueSalle();
        h.setSalle(salle);
        h.setAdmin(userService.getCurrentAdmin());
        h.setOperation(TypeOperation.MODIFICATION);
        historiqueSalleRepository.save(h);

        return mapToDTO(salle);
    }

    @Transactional
    public void toggleActivation(String id, boolean actif) {
        Salle salle = salleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Salle non trouvée"));
        salle.setActif(actif);
        salleRepository.save(salle);

        HistoriqueSalle h = new HistoriqueSalle();
        h.setSalle(salle);
        h.setAdmin(userService.getCurrentAdmin());
        h.setOperation(TypeOperation.ACTIVATION);
        historiqueSalleRepository.save(h);
    }

    @Transactional(readOnly = true)
    public List<SalleResponseDTO> findAll(boolean actif) {
        return salleRepository.findByActif(actif)
                .stream().map(this::mapToDTO).collect(Collectors.toList());
    }
}
