package com.example.cinimana.service;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;

    // ✅ Email de secours (corrigé : gmail.com au lieu de gmauil.com)
    private static final String FALLBACK_EMAIL = "safaa.analisse1@gmail.com";

    // Domaine interne généré automatiquement
    private static final String INTERNAL_EMAIL_DOMAIN = "@cinimana.internal";

    // Pattern pour valider un vrai email
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    );

    /**
     * Envoie un email avec les identifiants initiaux
     * Si l'email n'est pas valide ou est interne, utilise l'email de secours
     */
    public void sendInitialPasswordEmail(String toEmail, String initialPassword, String loginId) {

        String destinationEmail;
        boolean isRealEmail = isValidEmail(toEmail);

        // Déterminer l'email de destination
        if (!isRealEmail || toEmail.endsWith(INTERNAL_EMAIL_DOMAIN)) {
            destinationEmail = FALLBACK_EMAIL;
            logger.warn("Email invalide ou interne détecté: {}. Redirection vers: {}",
                    toEmail, FALLBACK_EMAIL);
        } else {
            destinationEmail = toEmail;
            logger.info("Envoi d'email vers l'adresse valide: {}", toEmail);
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(destinationEmail);
            message.setSubject("🎬 Bienvenue sur CiniMana - Vos Identifiants de Connexion");

            // Corps du message
            String text = buildEmailBody(toEmail, loginId, initialPassword, isRealEmail);
            message.setText(text);

            // Envoi
            mailSender.send(message);

            logger.info("✅ Email envoyé avec succès à: {}", destinationEmail);

        } catch (Exception e) {
            logger.error("❌ Erreur lors de l'envoi de l'email à {}: {}",
                    destinationEmail, e.getMessage(), e);
            throw new RuntimeException("Échec de l'envoi de l'email: " + e.getMessage());
        }
    }

    /**
     * Construit le corps de l'email selon le type d'adresse
     */
    private String buildEmailBody(String originalEmail, String loginId,
                                  String password, boolean isRealEmail) {

        StringBuilder body = new StringBuilder();

        body.append("Bonjour,\n\n");
        body.append("Votre compte CiniMana a été créé avec succès par l'administrateur.\n\n");
        body.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
        body.append("📋 VOS IDENTIFIANTS DE CONNEXION\n");
        body.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n\n");

        if (isRealEmail) {
            body.append("🔑 Email (Login) : ").append(originalEmail).append("\n");
        } else {
            body.append("🔑 ID Utilisateur (Login) : ").append(loginId).append("\n");
        }

        body.append("🔒 Mot de passe initial : ").append(password).append("\n\n");
        body.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n\n");

        body.append("⚠️ IMPORTANT - SÉCURITÉ\n");
        body.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
        body.append("• Vous DEVEZ changer ce mot de passe lors de votre première connexion\n");
        body.append("• Ne partagez JAMAIS vos identifiants\n");
        body.append("• Conservez ce mot de passe en lieu sûr\n\n");

        // Message spécifique pour email invalide/interne
        if (!isRealEmail) {
            body.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
            body.append("📧 NOTE ADMINISTRATIVE\n");
            body.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
            body.append("L'email de l'utilisateur (").append(originalEmail).append(") ");
            body.append("n'est pas une adresse valide.\n");
            body.append("Ce message a été envoyé à l'adresse de secours: ");
            body.append(FALLBACK_EMAIL).append("\n");
            body.append("Veuillez transmettre ces identifiants à l'utilisateur concerné.\n\n");
        }

        body.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
        body.append("🎬 Bienvenue dans l'équipe CiniMana !\n");
        body.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n\n");
        body.append("Cordialement,\n");
        body.append("L'équipe CiniMana\n");

        return body.toString();
    }

    /**
     * Valide le format d'un email
     */
    private boolean isValidEmail(String email) {
        if (email == null || email.isBlank()) {
            return false;
        }
        return EMAIL_PATTERN.matcher(email).matches();
    }

    /**
     * Envoie un email de réinitialisation de mot de passe
     */
    public void sendPasswordResetEmail(String toEmail, String userName, String resetToken) {

        String destinationEmail = isValidEmail(toEmail) ? toEmail : FALLBACK_EMAIL;

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(destinationEmail);
            message.setSubject("🔐 CiniMana - Réinitialisation de mot de passe");

            String text = String.format(
                    "Bonjour %s,\n\n" +
                            "Une demande de réinitialisation de mot de passe a été effectuée.\n\n" +
                            "Code de réinitialisation : %s\n\n" +
                            "Ce code expire dans 15 minutes.\n\n" +
                            "Si vous n'avez pas demandé cette réinitialisation, ignorez ce message.\n\n" +
                            "Cordialement,\n" +
                            "L'équipe CiniMana",
                    userName, resetToken
            );

            message.setText(text);
            mailSender.send(message);

            logger.info("✅ Email de réinitialisation envoyé à: {}", destinationEmail);

        } catch (Exception e) {
            logger.error("❌ Erreur envoi email réinitialisation: {}", e.getMessage());
            throw new RuntimeException("Échec de l'envoi de l'email");
        }
    }

    /**
     * Envoie une notification d'activation/désactivation de compte
     */
    public void sendAccountStatusEmail(String toEmail, String userName, boolean isActive) {

        String destinationEmail = isValidEmail(toEmail) ? toEmail : FALLBACK_EMAIL;

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(destinationEmail);

            if (isActive) {
                message.setSubject("✅ CiniMana - Compte Réactivé");
                message.setText(String.format(
                        "Bonjour %s,\n\n" +
                                "Votre compte CiniMana a été réactivé.\n\n" +
                                "Vous pouvez maintenant vous reconnecter.\n\n" +
                                "Cordialement,\n" +
                                "L'équipe CiniMana",
                        userName
                ));
            } else {
                message.setSubject("⚠️ CiniMana - Compte Désactivé");
                message.setText(String.format(
                        "Bonjour %s,\n\n" +
                                "Votre compte CiniMana a été désactivé.\n\n" +
                                "Pour plus d'informations, contactez l'administrateur.\n\n" +
                                "Cordialement,\n" +
                                "L'équipe CiniMana",
                        userName
                ));
            }

            mailSender.send(message);
            logger.info("✅ Email de statut de compte envoyé à: {}", destinationEmail);

        } catch (Exception e) {
            logger.error("❌ Erreur envoi email statut: {}", e.getMessage());
            // Ne pas faire échouer l'opération pour un problème d'email
        }
    }
}


//
//import org.springframework.mail.SimpleMailMessage;
//import org.springframework.mail.javamail.JavaMailSender;
//import org.springframework.stereotype.Service;
//
//@Service
//public class EmailService {
//
//    private final JavaMailSender mailSender;
//
//    public EmailService(JavaMailSender mailSender) {
//        this.mailSender = mailSender;
//    }
//
//    public void sendInitialPasswordEmail(String to, String password, String login) {
//        SimpleMailMessage message = new SimpleMailMessage();
//        message.setTo(to);
//        message.setSubject("Votre mot de passe initial");
//        message.setText("Bonjour,\n\nVotre login est : " + login +
//                "\nVotre mot de passe initial est : " + password +
//                "\n\nMerci.");
//
//        mailSender.send(message);
//    }
//}
