package com.example.cinimana.service;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import jakarta.mail.internet.MimeMessage;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.MimeMessageHelper;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;

    // ✅ Email de secours
    private static final String FALLBACK_EMAIL = "safaa.analisse1@gmail.com";

    // Domaine interne généré automatiquement
    private static final String INTERNAL_EMAIL_DOMAIN = "@cinimana.internal";

    // Pattern pour valider un vrai email
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

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
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(destinationEmail);
            helper.setSubject("🎬 Bienvenue sur Cinémana - Vos Identifiants");

            String htmlContent = buildWelcomeHtml(toEmail, loginId, initialPassword, isRealEmail);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            logger.info("✅ Email HTML de bienvenue envoyé à: {}", destinationEmail);

        } catch (Exception e) {
            logger.error("❌ Erreur envoi email bienvenue: {}", e.getMessage());
            throw new RuntimeException("Échec de l'envoi de l'email: " + e.getMessage());
        }
    }

    private String buildWelcomeHtml(String originalEmail, String loginId, String password, boolean isRealEmail) {
        String loginLabel = isRealEmail ? "EMAIL" : "ID CONNEXION";
        String loginValue = isRealEmail ? originalEmail : loginId;

        String warningNote = !isRealEmail
                ? "<div style='border-top:1px solid #e5e7eb; margin-top:20px; padding-top:10px; font-size:11px; color:#6b7280;'>"
                +
                "Note: Cet email a été envoyé à l'adresse de secours car l'adresse (" + originalEmail
                + ") est invalide." +
                "</div>"
                : "";

        return "<!DOCTYPE html><html><head><style>" +
                "  body { font-family: 'Segoe UI', Arial, sans-serif; background-color: #f4f4f4; margin: 0; padding: 0; }"
                +
                "  .container { max-width: 600px; margin: 20px auto; background-color: #ffffff; border-radius: 8px; overflow: hidden; box-shadow: 0 4px 10px rgba(0,0,0,0.1); }"
                +
                "  .header { background-color: #18181b; color: #ffffff; padding: 40px 20px; text-align: center; border-bottom: 4px solid #dc2626; }"
                +
                "  .content { padding: 40px; line-height: 1.6; color: #374151; }" +
                "  .card { background-color: #f9fafb; border: 1px solid #e5e7eb; padding: 25px; border-radius: 8px; margin: 25px 0; }"
                +
                "  .footer { background-color: #f9fafb; color: #9ca3af; padding: 20px; text-align: center; font-size: 12px; }"
                +
                "  .btn { display: inline-block; background-color: #dc2626; color: #ffffff !important; padding: 15px 30px; text-decoration: none; border-radius: 6px; font-weight: bold; }"
                +
                "  h1 { margin: 0; letter-spacing: 4px; }" +
                "  .label { font-size: 11px; font-weight: bold; color: #9ca3af; letter-spacing: 1px; }" +
                "  .value { font-size: 18px; font-weight: bold; color: #111827; margin-bottom: 15px; }" +
                "</style></head><body>" +
                "  <div class='container'>" +
                "    <div class='header'><h1>CINÉMANA</h1></div>" +
                "    <div class='content'>" +
                "      <h2 style='color:#111827; margin-top:0;'>Bienvenue dans l'équipe !</h2>" +
                "      <p>Votre compte a été créé avec succès. Voici vos identifiants pour accéder à la plateforme :</p>"
                +
                "      <div class='card'>" +
                "        <div class='label'>" + loginLabel + "</div>" +
                "        <div class='value'>" + loginValue + "</div>" +
                "        <div class='label'>MOT DE PASSE TEMPORAIRE</div>" +
                "        <div class='value'>" + password + "</div>" +
                "      </div>" +
                "      <div style='text-align:center; margin: 30px 0;'>" +
                "        <a href='#' class='btn'>SE CONNECTER AU DASHBOARD</a>" +
                "      </div>" +
                "      <p style='font-size:13px; color:#6b7280;'>⚠️ Pour votre sécurité, vous devrez changer ce mot de passe lors de votre première connexion.</p>"
                +
                "      " + warningNote + "" +
                "    </div>" +
                "    <div class='footer'><p>&copy; 2025 Cinémana Platform</p></div>" +
                "  </div>" +
                "</body></html>";
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
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(destinationEmail);
            helper.setSubject("🔐 Récupération de compte Cinémana");

            String htmlContent = buildResetPasswordHtml(userName, resetToken);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            logger.info("✅ Email HTML de reset envoyé à: {}", destinationEmail);

        } catch (Exception e) {
            logger.error("❌ Erreur envoi email reset: {}", e.getMessage());
            throw new RuntimeException("Échec de l'envoi de l'email");
        }
    }

    private String buildResetPasswordHtml(String userName, String resetToken) {
        return "<!DOCTYPE html><html><head><style>" +
                "  body { font-family: 'Segoe UI', Arial, sans-serif; background-color: #f4f4f4; margin: 0; padding: 0; }"
                +
                "  .container { max-width: 600px; margin: 20px auto; background-color: #ffffff; border-radius: 8px; overflow: hidden; box-shadow: 0 4px 10px rgba(0,0,0,0.1); }"
                +
                "  .header { background-color: #dc2626; color: #ffffff; padding: 30px; text-align: center; }" +
                "  .content { padding: 40px; line-height: 1.6; color: #374151; }" +
                "  .token-box { background-color: #fef2f2; border: 2px dashed #f87171; padding: 20px; text-align: center; border-radius: 8px; margin: 25px 0; }"
                +
                "  .token { font-size: 32px; font-weight: bold; color: #dc2626; letter-spacing: 5px; }" +
                "  .footer { background-color: #f9fafb; color: #9ca3af; padding: 20px; text-align: center; font-size: 12px; }"
                +
                "</style></head><body>" +
                "  <div class='container'>" +
                "    <div class='header'><h1 style='margin:0; letter-spacing:3px;'>CINÉMANA</h1></div>" +
                "    <div class='content'>" +
                "      <h2 style='color:#111827; margin-top:0;'>Réinitialisation du mot de passe</h2>" +
                "      <p>Bonjour " + userName + ",</p>" +
                "      <p>Vous avez demandé la réinitialisation de votre mot de passe. Utilisez le code ci-dessous pour continuer :</p>"
                +
                "      <div class='token-box'>" +
                "        <div class='token'>" + resetToken + "</div>" +
                "      </div>" +
                "      <p>Ce code est valable pendant <strong>15 minutes</strong>. Si vous n'êtes pas à l'origine de cette demande, vous pouvez ignorer cet email.</p>"
                +
                "    </div>" +
                "    <div class='footer'><p>&copy; 2025 Cinémana</p></div>" +
                "  </div>" +
                "</body></html>";
    }

    /**
     * Envoie une notification d'activation/désactivation de compte
     */
    public void sendAccountStatusEmail(String toEmail, String userName, boolean isActive) {

        String destinationEmail = isValidEmail(toEmail) ? toEmail : FALLBACK_EMAIL;

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(destinationEmail);
            String subject = isActive ? "✅ Votre compte Cinémana est actif"
                    : "⚠️ Information sur votre compte Cinémana";
            helper.setSubject(subject);

            String htmlContent = buildAccountStatusHtml(userName, isActive);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            logger.info("✅ Email HTML de statut envoyé à: {}", destinationEmail);

        } catch (Exception e) {
            logger.error("❌ Erreur envoi email statut: {}", e.getMessage());
        }
    }

    private String buildAccountStatusHtml(String userName, boolean isActive) {
        String statusTitle = isActive ? "Compte Réactivé" : "Compte Désactivé";
        String statusColor = isActive ? "#10b981" : "#f59e0b";
        String statusIcon = isActive ? "✅" : "⚠️";
        String statusMessage = isActive
                ? "Bonne nouvelle ! Votre accès à la plateforme Cinémana a été rétabli. Vous pouvez vous connecter dès maintenant."
                : "Votre compte a été temporairement désactivé par l'administrateur. Veuillez nous contacter pour plus d'informations.";

        return "<!DOCTYPE html><html><head><style>" +
                "  body { font-family: 'Segoe UI', Arial, sans-serif; background-color: #f4f4f4; margin: 0; padding: 0; }"
                +
                "  .container { max-width: 600px; margin: 20px auto; background-color: #ffffff; border-radius: 8px; overflow: hidden; box-shadow: 0 4px 10px rgba(0,0,0,0.1); }"
                +
                "  .header { background-color: #18181b; color: #ffffff; padding: 30px; text-align: center; border-bottom: 4px solid "
                + statusColor + "; }" +
                "  .content { padding: 40px; line-height: 1.6; color: #374151; }" +
                "  .status-badge { display: inline-block; padding: 8px 16px; border-radius: 20px; background-color: #f3f4f6; color: "
                + statusColor + "; font-weight: bold; margin-bottom: 20px; }" +
                "  .footer { background-color: #f9fafb; color: #9ca3af; padding: 20px; text-align: center; font-size: 12px; }"
                +
                "</style></head><body>" +
                "  <div class='container'>" +
                "    <div class='header'><h1 style='margin:0; letter-spacing:3px;'>CINÉMANA</h1></div>" +
                "    <div class='content'>" +
                "      <div class='status-badge'>" + statusIcon + " " + statusTitle.toUpperCase() + "</div>" +
                "      <h2 style='color:#111827; margin-top:0;'>Bonjour " + userName + ",</h2>" +
                "      <p>" + statusMessage + "</p>" +
                "      <p>Cordialement,<br>L'équipe Cinémana</p>" +
                "    </div>" +
                "    <div class='footer'><p>&copy; 2025 Cinémana</p></div>" +
                "  </div>" +
                "</body></html>";
    }

    /**
     * Envoie un email de confirmation de présence (3h avant la séance) en format
     * HTML
     */
    public void sendReservationConfirmationEmail(
            String toEmail,
            String clientName,
            String filmTitle,
            String seanceDateTime,
            String salleName,
            int nombrePlaces,
            String codeReservation,
            String confirmationLink) {

        String destinationEmail = isValidEmail(toEmail) ? toEmail : FALLBACK_EMAIL;

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(destinationEmail);
            helper.setSubject("⏰ CINÉMANA : Votre séance commence bientôt !");

            String htmlContent = buildPresenceConfirmationHtml(clientName, filmTitle, seanceDateTime, salleName,
                    nombrePlaces, codeReservation, confirmationLink);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            logger.info("✅ Email HTML de rappel (3h) envoyé à: {} pour code: {}", destinationEmail, codeReservation);

        } catch (Exception e) {
            logger.error("❌ Erreur envoi email rappel (HTML): {}", e.getMessage());
        }
    }

    private String buildPresenceConfirmationHtml(String clientName, String filmTitle, String seanceDateTime,
                                                 String salleName, int nombrePlaces, String codeReservation, String confirmationLink) {
        return "<!DOCTYPE html>" +
                "<html>" +
                "<head><style>" +
                "  body { font-family: 'Segoe UI', Arial, sans-serif; background-color: #f4f4f4; color: #333; margin: 0; padding: 0; }"
                +
                "  .container { max-width: 600px; margin: 20px auto; background-color: #ffffff; border-radius: 8px; overflow: hidden; box-shadow: 0 4px 10px rgba(0,0,0,0.1); }"
                +
                "  .header { background-color: #18181b; color: #ffffff; padding: 30px; text-align: center; border-bottom: 4px solid #dc2626; }"
                +
                "  .content { padding: 30px; line-height: 1.6; }" +
                "  .info-box { background-color: #fff1f2; border: 1px solid #fecaca; padding: 20px; border-radius: 6px; margin: 20px 0; }"
                +
                "  .footer { background-color: #f9fafb; color: #6b7280; padding: 20px; text-align: center; font-size: 12px; }"
                +
                "  .btn { display: inline-block; background-color: #dc2626; color: #ffffff !important; padding: 15px 30px; text-decoration: none; border-radius: 6px; font-weight: bold; margin-top: 25px; }"
                +
                "  .warning { color: #991b1b; font-weight: bold; border-top: 1px solid #fecaca; padding-top: 15px; margin-top: 15px; }"
                +
                "</style></head>" +
                "<body>" +
                "  <div class='container'>" +
                "    <div class='header'><h1>CINÉMANA</h1></div>" +
                "    <div class='content'>" +
                "      <h2>Bonjour " + clientName + ",</h2>" +
                "      <p>Votre séance pour <strong>" + filmTitle.toUpperCase()
                + "</strong> commence dans exactement <strong>3 heures</strong> !</p>" +
                "      <div class='info-box'>" +
                "        <p style='margin: 0;'><strong>🎬 Détails</strong></p>" +
                "        <p style='margin: 5px 0;'>Séance : " + seanceDateTime + "</p>" +
                "        <p style='margin: 5px 0;'>Salle : " + salleName + "</p>" +
                "        <p style='margin: 5px 0;'>Places : " + nombrePlaces + "</p>" +
                "        <p class='warning'>⚠️ ACTION REQUISE : Veuillez confirmer votre présence en cliquant sur le bouton ci-dessous dans l'HEURE qui suit pour conserver votre réservation.</p>"
                +
                "      </div>" +
                "      <div style='text-align: center;'>" +
                "        <a href='" + confirmationLink + "' class='btn'>CONFIRMER MA PRÉSENCE</a>" +
                "      </div>" +
                "      <p style='text-align: center; font-size: 11px; color: #991b1b; margin-top: 10px;'>Si vous ne confirmez pas, votre réservation sera automatiquement annulée.</p>"
                +
                "    </div>" +
                "    <div class='footer'>" +
                "      <p>&copy; 2025 Cinémana. À tout de suite au cinéma !</p>" +
                "    </div>" +
                "  </div>" +
                "</body>" +
                "</html>";
    }

    /**
     * Envoie un email de confirmation de réservation réussie avec le billet PDF en
     * pièce jointe
     */
    public void sendReservationSuccessEmailWithAttachment(
            String toEmail,
            String clientName,
            String filmTitle,
            String seanceDateTime,
            String codeReservation,
            byte[] pdfAttachment) {

        String destinationEmail = isValidEmail(toEmail) ? toEmail : FALLBACK_EMAIL;

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(destinationEmail);
            helper.setSubject("🎬 Votre Billet Cinémana : " + filmTitle);

            String htmlContent = buildReservationHtml(clientName, filmTitle, seanceDateTime, codeReservation);
            helper.setText(htmlContent, true);

            // Ajout du PDF en pièce jointe
            if (pdfAttachment != null) {
                helper.addAttachment("Billet_" + codeReservation + ".pdf", new ByteArrayResource(pdfAttachment));
            }

            mailSender.send(message);
            logger.info("✅ Email HTML avec pièce jointe envoyé à: {}", destinationEmail);

        } catch (Exception e) {
            logger.error("❌ Erreur envoi email succès réservation (HTML): {}", e.getMessage());
            // Fallback sur l'email simple si l'HTML échoue (optionnel, mais ici on log
            // juste)
        }
    }

    private String buildReservationHtml(String clientName, String filmTitle, String seanceDateTime,
                                        String codeReservation) {
        return "<!DOCTYPE html>" +
                "<html>" +
                "<head>" +
                "<style>" +
                "  body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; background-color: #f4f4f4; color: #333; margin: 0; padding: 0; }"
                +
                "  .container { max-width: 600px; margin: 20px auto; background-color: #ffffff; border-radius: 8px; overflow: hidden; box-shadow: 0 4px 6px rgba(0,0,0,0.1); }"
                +
                "  .header { background-color: #dc2626; color: #ffffff; padding: 30px; text-align: center; }" +
                "  .content { padding: 30px; line-height: 1.6; }" +
                "  .movie-card { background-color: #f9f9f9; border-left: 4px solid #dc2626; padding: 20px; margin: 20px 0; border-radius: 4px; }"
                +
                "  .footer { background-color: #18181b; color: #a1a1aa; padding: 20px; text-align: center; font-size: 12px; }"
                +
                "  .btn { display: inline-block; background-color: #dc2626; color: #ffffff; padding: 12px 25px; text-decoration: none; border-radius: 5px; font-weight: bold; margin-top: 20px; }"
                +
                "  h1 { margin: 0; font-size: 28px; letter-spacing: 2px; }" +
                "  .code { font-size: 20px; font-weight: bold; color: #dc2626; }" +
                "</style>" +
                "</head>" +
                "<body>" +
                "  <div class='container'>" +
                "    <div class='header'>" +
                "      <h1>CINÉMANA</h1>" +
                "      <p style='margin-top: 10px; opacity: 0.9;'>L'EXPÉRIENCE ULTIME DU CINÉMA</p>" +
                "    </div>" +
                "    <div class='content'>" +
                "      <h2 style='color: #18181b;'>Félicitations " + clientName + " !</h2>" +
                "      <p>Votre réservation a été confirmée avec succès. Préparez le pop-corn, une séance inoubliable vous attend !</p>"
                +
                "      <div class='movie-card'>" +
                "        <p style='margin: 0; font-size: 12px; color: #666; font-weight: bold;'>FILM</p>" +
                "        <p style='margin: 5px 0 15px 0; font-size: 22px; font-weight: bold; color: #18181b;'>"
                + filmTitle.toUpperCase() + "</p>" +
                "        <p style='margin: 0; font-size: 12px; color: #666; font-weight: bold;'>SÉANCE</p>" +
                "        <p style='margin: 5px 0 0 0; font-size: 16px; color: #333;'>" + seanceDateTime + "</p>" +
                "      </div>" +
                "      <p>Votre code de réservation est : <span class='code'>" + codeReservation + "</span></p>" +
                "      <p><b>📧 Note :</b> Votre billet PDF est joint à cet email. Vous pouvez également le retrouver à tout moment dans votre espace client.</p>"
                +
                "      <p>À très bientôt dans nos salles !</p>" +
                "      <div style='text-align: center;'>" +
                "        <a href='#' class='btn'>Accéder à mon compte</a>" +
                "      </div>" +
                "    </div>" +
                "    <div class='footer'>" +
                "      <p>&copy; 2025 Cinémana. Tous droits réservés.</p>" +
                "      <p>Ceci est un email automatique, merci de ne pas y répondre.</p>" +
                "    </div>" +
                "  </div>" +
                "</body>" +
                "</html>";
    }

    /**
     * Envoie un email de confirmation de réservation réussie (après création)
     *
     * @deprecated Utiliser sendReservationSuccessEmailWithAttachment pour l'envoi
     *             du PDF
     */
    @Deprecated
    public void sendReservationSuccessEmail(
            String toEmail,
            String clientName,
            String filmTitle,
            String seanceDateTime,
            String codeReservation) {
        // ... (keep existing implementation or call the new one with null attachment)
        sendReservationSuccessEmailWithAttachment(toEmail, clientName, filmTitle, seanceDateTime, codeReservation,
                null);
    }
}
