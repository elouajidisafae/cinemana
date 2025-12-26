package com.example.cinimana.service.client;

import com.example.cinimana.dto.request.ChangePasswordRequest;
import com.example.cinimana.dto.request.ClientProfileRequest;
import com.example.cinimana.model.Client;
import com.example.cinimana.model.Reservation;
import com.example.cinimana.repository.ClientRepository;
import com.example.cinimana.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ClientService {
//
    private final ReservationRepository reservationRepository;
    private final ClientRepository clientRepository;

    private final PasswordEncoder passwordEncoder;

    public List<Reservation> getMyReservations(String email) {
        Client client = clientRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Client non trouvé"));

        // Fetch all reservations for the client
        List<Reservation> allReservations = reservationRepository
                .findByClientIdOrderByDateReservationDesc(client.getId());

        // Filter out reservations where the film is deactivated (actif = false)
        return allReservations.stream()
                .filter(reservation -> reservation.getSeance().getFilm().isActif())
                .collect(Collectors.toList());
    }

    public Client getProfile(String email) {
        return clientRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Client non trouvé"));
    }

    public Client updateProfile(String email, ClientProfileRequest request) {
        Client client = clientRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Client non trouvé"));

        client.setNom(request.getNom());
        client.setPrenom(request.getPrenom());
        client.setNumeroTelephone(request.getNumeroTelephone());

        return clientRepository.save(client);
    }

    public void updatePassword(String email, ChangePasswordRequest request) {
        Client client = clientRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Client non trouvé"));

        if (!passwordEncoder.matches(request.getCurrentPassword(), client.getMotDePasse())) {
            throw new RuntimeException("L'ancien mot de passe est incorrect");
        }

        client.setMotDePasse(passwordEncoder.encode(request.getNewPassword()));
        clientRepository.save(client);
    }
}
