package com.example.cinimana.service;

import com.example.cinimana.repository.UtilisateurRepository;
import com.example.cinimana.repository.SalleRepository;
import com.example.cinimana.repository.FilmRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
public class IdGeneratorService {

    private final UtilisateurRepository utilisateurRepository;
    private final SalleRepository salleRepository;
    private final FilmRepository filmRepository;

    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int ID_LENGTH = 10;
    private final SecureRandom random = new SecureRandom();

    private String generateRandomId() {
        return IntStream.range(0, ID_LENGTH)
                .mapToObj(i -> CHARACTERS.charAt(random.nextInt(CHARACTERS.length())))
                .map(Object::toString)
                .collect(Collectors.joining());
    }

    public String generateUniqueIdForUtilisateur() {
        String id;
        do {
            id = generateRandomId();
        } while (utilisateurRepository.existsById(id));
        return id;
    }

    public String generateUniqueIdForSalle() {
        String id;
        do {
            id = generateRandomId();
        } while (salleRepository.existsById(id));
        return id;
    }

    public String generateUniqueIdForFilm() {
        String id;
        do {
            id = generateRandomId();
        } while (filmRepository.existsById(id));
        return id;
    }
}
