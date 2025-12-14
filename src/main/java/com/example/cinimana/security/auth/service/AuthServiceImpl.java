package com.example.cinimana.security.auth.service;

import com.example.cinimana.model.Client;
import com.example.cinimana.model.Role;
import com.example.cinimana.model.Utilisateur; // Import nécessaire
import com.example.cinimana.repository.AdminRepository;
import com.example.cinimana.repository.ClientRepository;
import com.example.cinimana.repository.UtilisateurRepository;
import com.example.cinimana.security.auth.dto.AuthRequest;
import com.example.cinimana.security.auth.dto.AuthResponse;
import com.example.cinimana.security.auth.dto.RegisterRequest;
import com.example.cinimana.security.jwt.JwtService;
import com.example.cinimana.security.user.CustomUserDetailsService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthServiceImpl.class);

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final CustomUserDetailsService userDetailsService;
    private final PasswordEncoder passwordEncoder;
    private final ClientRepository clientRepository;
    private final AdminRepository adminRepository;
    private final UtilisateurRepository utilisateurRepository;

    // Login client uniquement
    @Override
    public AuthResponse loginClient(AuthRequest request) {
        UserDetails userDetails = userDetailsService.loadUserByUsername(request.email());

        // Vérifier que c'est bien un client
        if (!userDetails.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_CLIENT"))) {
            logger.warn("Tentative de login client échouée pour un utilisateur non-client: {}", request.email());
            throw new RuntimeException("Seuls les clients peuvent se connecter ici");
        }

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.email(), request.motDePasse())
            );
            logger.info("Connexion client réussie pour email: {}", request.email());
        } catch (Exception ex) {
            logger.warn("Tentative de connexion client échouée pour email: {}", request.email());
            throw ex;
        }

        String token = jwtService.generateToken(userDetails);

        // Clients (ID: Long dans l'entité, doit être String dans la réponse)
        return new AuthResponse(
                token,
                "CLIENT",
                getNomComplet(request.email()),
                request.email(), // ✅ Email
                getUserId(request.email()),
                false // ✅ Ajout du flag pour le constructeur, false pour les clients
        );
    }

    // Login utilisateurs internes : admin, commercial, caissier
    @Override
    public AuthResponse loginInternal(AuthRequest request) {
        UserDetails userDetails = userDetailsService.loadUserByUsername(request.email());

        // Empêcher les clients de se connecter ici
        if (userDetails.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_CLIENT"))) {
            logger.warn("Tentative de login interne échouée pour un client: {}", request.email());
            throw new RuntimeException("Les clients ne peuvent pas utiliser ce login");
        }

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.email(), request.motDePasse())
            );
            logger.info("Connexion interne réussie pour email: {}", request.email());
        } catch (Exception ex) {
            logger.warn("Tentative de connexion interne échouée pour email: {}", request.email());
            throw ex;
        }

        String role = userDetails.getAuthorities().iterator().next().getAuthority().replace("ROLE_", "");
        String token = jwtService.generateToken(userDetails);

        boolean isFirstLogin = getPremiereConnexionStatus(request.email());

        return new AuthResponse(
                token,
                role,
                getNomComplet(request.email()),
                request.email(), // ✅ Email
                getUserId(request.email()),
                isFirstLogin // ✅ Ajout du flag pour la redirection frontend
        );
    }

    // Enregistrement client (inchangé)
    @Override
    public AuthResponse register(RegisterRequest request) {
        if (clientRepository.existsByEmail(request.email()) ||
                utilisateurRepository.existsByEmail(request.email()) ||
                adminRepository.existsByEmail(request.email())) {
            logger.warn("Tentative d'inscription avec email déjà utilisé: {}", request.email());
            throw new RuntimeException("Email déjà utilisé");
        }

        // Vérification force mot de passe
        if (!request.motDePasse().matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&]).{8,}$")) {
            logger.warn("Mot de passe trop faible pour email: {}", request.email());
            throw new RuntimeException(
                    "Mot de passe trop faible. Doit contenir min 8 caractères, maj, min, chiffre et spécial."
            );
        }

        Client client = new Client();
        client.setNom(request.nom());
        client.setPrenom(request.prenom());
        client.setEmail(request.email());
        client.setMotDePasse(passwordEncoder.encode(request.motDePasse()));
        client.setNumeroTelephone(request.numeroTelephone());
        client.setDateNaissance(request.dateNaissance());
        client.setRole(Role.CLIENT);
        clientRepository.save(client);

        logger.info("Nouvel utilisateur client enregistré avec email: {}", request.email());
        AuthResponse response = loginClient(new AuthRequest(request.email(), request.motDePasse()));
        return response;
    }

    // --- Méthodes utilitaires ---

    private boolean getPremiereConnexionStatus(String email) {
        // Retourne le flag de l'utilisateur interne, ou false s'il n'existe pas ou est Admin
        return utilisateurRepository.findByEmail(email)
                .map(Utilisateur::isPremiereConnexion)
                .orElse(false);
    }

    private String getNomComplet(String email) {
        return adminRepository.findByEmail(email)
                .map(a -> a.getNom() + " " + a.getPrenom())
                .or(() -> utilisateurRepository.findByEmail(email)
                        .map(u -> u.getNom() + " " + u.getPrenom()))
                .or(() -> clientRepository.findByEmail(email)
                        .map(c -> c.getNom() + " " + c.getPrenom()))
                .orElse("Utilisateur");
    }

    // ✅ CORRECTION : Le type de retour est maintenant String
    private String getUserId(String email) {

        // 1. Tenter de trouver l'Admin
        Optional<String> adminId = adminRepository.findByEmail(email)
                // L'ID de l'Admin est Long, on le convertit en String
                .map(a -> String.valueOf(a.getId()));

        if (adminId.isPresent()) {
            return adminId.get();
        }

        // 2. Tenter de trouver l'Utilisateur Interne (Commercial/Caissier)
        // u.getId() est maintenant un String (grâce à la correction de l'entité)
        Optional<String> utilisateurId = utilisateurRepository.findByEmail(email)
                .map(Utilisateur::getId); // Map sur le String ID

        if (utilisateurId.isPresent()) {
            return utilisateurId.get();
        }

        // 3. Tenter de trouver le Client
        Optional<String> clientId = clientRepository.findByEmail(email)
                // L'ID du Client est Long, on le convertit en String
                .map(c -> String.valueOf(c.getId()));

        if (clientId.isPresent()) {
            return clientId.get();
        }

        return null;
    }
}