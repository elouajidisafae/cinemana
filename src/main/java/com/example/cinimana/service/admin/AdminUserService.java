package com.example.cinimana.service.admin;

import com.example.cinimana.dto.request.UtilisateurRequestDTO;
import com.example.cinimana.dto.response.UtilisateurResponseDTO;
import com.example.cinimana.exception.NotFoundException;
import com.example.cinimana.model.*;
import com.example.cinimana.repository.UtilisateurRepository;
import com.example.cinimana.repository.HistoriqueUtilisateurRepository;
import com.example.cinimana.service.UserService;
import com.example.cinimana.service.EmailService;
import com.example.cinimana.service.PasswordGeneratorService;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class AdminUserService {

    private static final Logger logger = LoggerFactory.getLogger(AdminUserService.class);

    // Pattern de validation du mot de passe
    private static final Pattern PASSWORD_PATTERN = Pattern.compile(
            "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&]).{8,}$");

    private final UtilisateurRepository utilisateurRepository;
    private final HistoriqueUtilisateurRepository historiqueUtilisateurRepository;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final PasswordGeneratorService passwordGeneratorService;

    // --- UTILS : LOG & MAPPING ---
    private void logHistorique(Utilisateur user, TypeOperation operation) {// Méthode utilitaire pour logger l'historique des opérations
        Admin currentAdmin = userService.getCurrentAdmin();
        HistoriqueUtilisateur historique = new HistoriqueUtilisateur();
        historique.setUtilisateur(user);
        historique.setAdmin(currentAdmin);
        historique.setOperation(operation);
        historiqueUtilisateurRepository.save(historique);
    }

    private UtilisateurResponseDTO mapToUtilisateurResponseDTO(Utilisateur user) {
        return new UtilisateurResponseDTO(
                user.getId(),
                user.getNom(),
                user.getPrenom(),
                user.getEmail(),
                user.getCin(),
                user.getDateNaissance(),
                user.getDateEmbauche(),
                user.getRole(),
                user.isActif(),
                user.isPremiereConnexion());
    }

    // --- 1. CRUD : AJOUT ---
    @Transactional
    public UtilisateurResponseDTO ajouterUtilisateur(UtilisateurRequestDTO dto) {

        // Validation de l'email unique
        if (utilisateurRepository.existsByEmail(dto.email())) {
            throw new RuntimeException("Cet email est déjà utilisé.");
        }

        // Gestion du mot de passe
        String rawPassword;
        boolean passwordWasGenerated = false;

        if (dto.motDePasse() == null || dto.motDePasse().isBlank()) {
            // Générer un mot de passe pour Commercial/Caissier
            if (dto.role() == Role.ADMIN) {
                throw new RuntimeException("Le mot de passe de l'Admin doit être défini.");
            }
            rawPassword = passwordGeneratorService.generateRandomPassword();
            passwordWasGenerated = true;
            logger.info("Mot de passe généré pour l'utilisateur: {}", dto.email());
        } else {
            // Valider le mot de passe fourni
            if (!PASSWORD_PATTERN.matcher(dto.motDePasse()).matches()) {
                throw new RuntimeException(
                        "Le mot de passe doit contenir au moins 8 caractères, " +
                                "une majuscule, une minuscule, un chiffre et un caractère spécial (@$!%*?&)");
            }
            rawPassword = dto.motDePasse();
        }

        // Création de l'utilisateur selon son rôle
        Utilisateur user = switch (dto.role()) {
            case COMMERCIAL -> new Commercial();
            case CAISSIER -> new Caissier();
            case ADMIN, CLIENT -> throw new IllegalArgumentException(
                    "Le rôle " + dto.role() + " ne peut pas être créé via cette API");
        };

        // Remplissage des données
        // L'ID sera généré automatiquement par le listener @PrePersist qui se trouve dans la classe Utilisateur
        user.setNom(dto.nom());
        user.setPrenom(dto.prenom());
        user.setEmail(dto.email());
        user.setCin(dto.cin());
        user.setDateNaissance(dto.dateNaissance());
        user.setDateEmbauche(dto.dateEmbauche());
        user.setMotDePasse(passwordEncoder.encode(rawPassword));
        user.setRole(dto.role());
        user.setActif(true);
        user.setPremiereConnexion(passwordWasGenerated);

        // Sauvegarde (déclenchera le @PrePersist pour générer l'ID)
        utilisateurRepository.save(user);

        logger.info("Utilisateur créé avec succès - ID: {}, Email: {}", user.getId(), user.getEmail());

        // Envoi d'email si mot de passe généré
        if (passwordWasGenerated) {
            try {
                emailService.sendInitialPasswordEmail(dto.email(), rawPassword, user.getId());
                logger.info("Email envoyé à: {}", dto.email());
            } catch (Exception e) {
                logger.error("Erreur lors de l'envoi de l'email à {}: {}", dto.email(), e.getMessage());
                // Ne pas faire échouer la transaction pour un problème d'email
            }
        }

        logHistorique(user, TypeOperation.CREATION);
        return mapToUtilisateurResponseDTO(user); // Retourner le DTO de réponse
    }

    //  MODIFICATION
    @Transactional   // Assure la cohérence de la transaction
    public UtilisateurResponseDTO modifierUtilisateur(String id, UtilisateurRequestDTO dto) {

        Utilisateur user = utilisateurRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Utilisateur non trouvé avec ID: " + id));

        // Mise à jour des champs modifiables
        user.setNom(dto.nom());
        user.setPrenom(dto.prenom());
        user.setCin(dto.cin());
        user.setDateNaissance(dto.dateNaissance());
        user.setDateEmbauche(dto.dateEmbauche());

        // Modification de l'email si différent
        if (!user.getEmail().equals(dto.email())) {
            if (utilisateurRepository.existsByEmail(dto.email())) {
                throw new RuntimeException("Cet email est déjà utilisé.");
            }
            user.setEmail(dto.email());
        } //bien que le backend permette techniquement la modification de l'email avec les validations nécessaires,
        // j'ai fait le choix de ne pas exposer cette fonctionnalité dans l'interface Frontend.

        user.setRole(dto.role());

        // Note: Le mot de passe n'est plus modifiable via cette méthode
        // Pour réinitialiser le mot de passe, utiliser la réactivation

        utilisateurRepository.save(user);
        logHistorique(user, TypeOperation.MODIFICATION);

        return mapToUtilisateurResponseDTO(user);
    }

    // --- 3. ACTIVATION/DESACTIVATION ---
    @Transactional
    public void toggleActivation(String id, boolean nouvelEtat) {

        Utilisateur user = utilisateurRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Utilisateur non trouvé avec ID: " + id));

        // Empêcher la désactivation d'un admin
        if (user.getRole() == Role.ADMIN && !nouvelEtat) {
            throw new IllegalStateException("Impossible de désactiver un compte administrateur");
        }

        if (user.isActif() != nouvelEtat) {
            user.setActif(nouvelEtat);

            if (nouvelEtat) {
                // Réactivation : générer un nouveau mot de passe et envoyer par email
                String newPassword = passwordGeneratorService.generateRandomPassword();
                user.setMotDePasse(passwordEncoder.encode(newPassword));
                user.setPremiereConnexion(true);

                logger.info("Utilisateur réactivé avec nouveau mot de passe: {}", id);

                // Envoyer l'email avec le nouveau mot de passe
                try {
                    emailService.sendInitialPasswordEmail(user.getEmail(), newPassword, user.getId());
                    logger.info("Email de réactivation envoyé à: {}", user.getEmail());
                } catch (Exception e) {
                    logger.error("Erreur lors de l'envoi de l'email de réactivation à {}: {}", user.getEmail(),
                            e.getMessage());
                    // Ne pas faire échouer la transaction pour un problème d'email
                }
            } else {
                logger.info("Utilisateur désactivé: {}", id);
            }

            utilisateurRepository.save(user);

            TypeOperation operation = nouvelEtat ? TypeOperation.ACTIVATION : TypeOperation.SUPPRESSION;
            logHistorique(user, operation);
        }
    }

    // 4. CONSULTATION
    @Transactional(readOnly = true) // on fait read only pour optimiser les performances pour que hibernate desactive le mecanisme de dirty checking
    public List<UtilisateurResponseDTO> findAllUsers(Boolean actif, Role role) {
        return utilisateurRepository.findUsersByFilters(actif, role)
                .stream()// convertit la liste en flux pour le traitement
                .map(this::mapToUtilisateurResponseDTO)// applique la fonction de mappage à chaque utilisateur
                .collect(Collectors.toList());// collecte les résultats dans une liste
    }

    // --- 5. RESET MOT DE PASSE INITIAL ---
    @Transactional // Assure la cohérence de la transaction
    public void performInitialPasswordResetByEmail(String email, String newPassword) {
        Utilisateur user = utilisateurRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé avec email: " + email));

        // Validation du mot de passe
        if (!PASSWORD_PATTERN.matcher(newPassword).matches()) {
            throw new RuntimeException(
                    "Le mot de passe doit contenir au moins 8 caractères, " +
                            "une majuscule, une minuscule, un chiffre et un caractère spécial");
        }

        //  Encoder le mot de passe avec BCrypt
        user.setMotDePasse(passwordEncoder.encode(newPassword)); // Assure-toi que passwordEncoder est injecté

        //  Mettre premiereConnexion à false
        user.setPremiereConnexion(false);

        //  Sauvegarder immédiatement dans la base
        utilisateurRepository.saveAndFlush(user); // flush pour forcer la mise à jour

        logger.info("Mot de passe initial réinitialisé pour l'utilisateur: {}", email);
    }
}