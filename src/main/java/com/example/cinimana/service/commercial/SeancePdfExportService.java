package com.example.cinimana.service.commercial;

import com.example.cinimana.dto.commercial.response.SeanceResponseDTO;
import com.itextpdf.kernel.colors.Color;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.element.Text;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.itextpdf.layout.properties.VerticalAlignment;
import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.kernel.font.PdfFontFactory;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Service d'export PDF - Design Fiche Cinéma Prestige (Fond Clair, Rouge/Or,
 * Simple et Structuré)
 */
@Service
@RequiredArgsConstructor
public class SeancePdfExportService {

    private static final Logger logger = LoggerFactory.getLogger(SeancePdfExportService.class);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final DateTimeFormatter DATE_ONLY_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yy");
    private static final DateTimeFormatter TIME_ONLY_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    // Palette de couleurs ajustée pour un fond clair
    private static final Color DEEP_RED = new DeviceRgb(139, 0, 0); // #8B0000 (Lignes structurelles / En-tête)
    private static final Color CRIMSON = new DeviceRgb(220, 20, 60); // #DC143C (Accentuation/Alerte)
    private static final Color GOLD = new DeviceRgb(255, 180, 0); // #FFB400 (Prix / Sous-titre - Légèrement
    // assombri pour le fond clair)

    // NOUVELLES COULEURS
    private static final Color PAPER_WHITE = new DeviceRgb(255, 255, 255); // Fond de page principal
    private static final Color DARK_TEXT = new DeviceRgb(30, 30, 30); // Texte principal foncé
    private static final Color MID_GRAY = new DeviceRgb(100, 100, 100); // Texte secondaire
    private static final Color LIGHT_BG = new DeviceRgb(245, 245, 245); // Fond d'alternance très clair

    public byte[] exportSeancesToPdf(List<SeanceResponseDTO> seances) {
        logger.info("Export PDF Fiche Cinéma Prestige (Fond Clair) de {} séances", seances.size());

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            document.setMargins(40, 40, 40, 40);
            document.setBackgroundColor(PAPER_WHITE); // Fond de page Blanc

            // Créer le conteneur principal (ici, on s'assure qu'il est blanc et bien
            // borduré)
            Table mainContainer = new Table(1);
            mainContainer.setWidth(UnitValue.createPercentValue(100));
            mainContainer.setBackgroundColor(PAPER_WHITE);
            mainContainer.setBorder(new SolidBorder(DEEP_RED, 2)); // Bordure fine Rouge

            Cell contentCell = new Cell();
            contentCell.setBorder(Border.NO_BORDER);
            contentCell.setPadding(30);
            contentCell.setBackgroundColor(PAPER_WHITE); // Fond du contenu en Blanc

            // =================================================================
            // 1. EN-TÊTE PRESTIGE
            // =================================================================

            Paragraph mainTitle = new Paragraph("C I N E M A N A")
                    .setFont(PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD))
                    .setFontSize(28)
                    .setFontColor(DEEP_RED) // Titre en DEEP_RED
                    .setTextAlignment(TextAlignment.LEFT)
                    .setMarginTop(0)
                    .setMarginBottom(5);
            contentCell.add(mainTitle);

            Paragraph subtitle = new Paragraph("PROGRAMME COMMERCIAL")
                    .setFont(PdfFontFactory.createFont(StandardFonts.TIMES_ITALIC))
                    .setFontSize(12)
                    .setFontColor(DARK_TEXT) // Sous-titre en texte foncé
                    .setTextAlignment(TextAlignment.LEFT)
                    .setMarginBottom(15);
            contentCell.add(subtitle);

            // Ligne de séparation GRANDE
            contentCell.add(new Table(1).setWidth(UnitValue.createPercentValue(100))
                    .setBorder(new SolidBorder(DEEP_RED, 2.5f)).setHeight(3).setMarginBottom(20));

            // =================================================================
            // 2. INFORMATIONS GÉNÉRALES
            // =================================================================

            Table infoTable = new Table(new float[] { 1f, 1f });
            infoTable.setWidth(UnitValue.createPercentValue(100));
            infoTable.setMarginBottom(20);

            // 1. GÉNÉRÉ LE (Gauche)
            Paragraph pGenere = new Paragraph()
                    .add(new Text("GÉNÉRÉ LE : ")
                            .setFont(PdfFontFactory
                                    .createFont(StandardFonts.HELVETICA_BOLD))
                            .setFontSize(9).setFontColor(MID_GRAY))
                    .add(new Text(LocalDateTime.now().format(DATE_FORMATTER))
                            .setFont(PdfFontFactory.createFont(StandardFonts.HELVETICA))
                            .setFontSize(9).setFontColor(DARK_TEXT));

            Cell genereCell = new Cell().add(pGenere)
                    .setBackgroundColor(PAPER_WHITE)
                    .setTextAlignment(TextAlignment.LEFT)
                    .setPadding(0)
                    .setBorder(Border.NO_BORDER);

            // 2. TOTAL SÉANCES (Droite)
            Paragraph pTotal = new Paragraph()
                    .add(new Text("TOTAL SÉANCES : ")
                            .setFont(PdfFontFactory
                                    .createFont(StandardFonts.HELVETICA_BOLD))
                            .setFontSize(9).setFontColor(MID_GRAY))
                    .add(new Text(String.valueOf(seances.size()))
                            .setFont(PdfFontFactory.createFont(StandardFonts.HELVETICA))
                            .setFontSize(9).setFontColor(DARK_TEXT));

            Cell totalCell = new Cell().add(pTotal)
                    .setBackgroundColor(PAPER_WHITE)
                    .setTextAlignment(TextAlignment.RIGHT)
                    .setPadding(0)
                    .setBorder(Border.NO_BORDER);

            infoTable.addCell(genereCell);
            infoTable.addCell(totalCell);

            contentCell.add(infoTable);

            // =================================================================
            // 3. TABLEAU PRINCIPAL (Fond d'en-tête Rouge)
            // =================================================================

            float[] columnWidths = { 2f, 3.5f, 1.5f, 1.2f, 1.2f, 1.2f, 1.2f };
            Table table = new Table(UnitValue.createPercentArray(columnWidths));
            table.setWidth(UnitValue.createPercentValue(100));
            table.setMarginTop(10);
            table.setBorder(Border.NO_BORDER);

            // En-têtes : Fond DEEP_RED, Texte BLANC gras
            addPrestigeHeader(table, "DATE & HEURE");
            addPrestigeHeader(table, "FILM / GENRE");
            addPrestigeHeader(table, "SALLE");
            addPrestigeHeader(table, "CAP.");
            addPrestigeHeader(table, "RÉSERV.");
            addPrestigeHeader(table, "DISPO.");
            addPrestigeHeader(table, "PRIX");

            // Données : Alternance subtile de fond blanc/gris très clair
            boolean isLight = true;
            for (SeanceResponseDTO seance : seances) {
                Color bgColor = isLight ? PAPER_WHITE : LIGHT_BG; // Alternance Blanc/Gris très clair
                Border cellBorder = new SolidBorder(MID_GRAY, 0.25f); // Bordure très fine GRIS entre
                // les lignes

                // 1. DATE & HEURE
                // Date en texte foncé, Heure en CRIMSON
                addPrestigeDateTimeCell(table, seance.dateHeure(), bgColor, DARK_TEXT, CRIMSON,
                        TextAlignment.LEFT, cellBorder);

                // 2. FILM
                // Titre en texte foncé (gras), Genre en MID_GRAY
                addPrestigeFilmCell(table, seance.filmTitre(), seance.filmGenre(), bgColor, DARK_TEXT,
                        MID_GRAY, TextAlignment.LEFT, cellBorder);

                // 3. SALLE
                addPrestigeCell(table, seance.salleNom(), bgColor, DARK_TEXT, TextAlignment.CENTER,
                        false, cellBorder);

                // 4. CAPACITÉ
                addPrestigeCell(table, String.valueOf(seance.salleCapacite()), bgColor, DARK_TEXT,
                        TextAlignment.CENTER, false, cellBorder);

                // 5. RÉSERVÉES
                double fillRate = (double) seance.placesReservees() / seance.salleCapacite();
                Color reservedColor = fillRate > 0.6 ? CRIMSON
                        : (fillRate > 0.3 ? DEEP_RED : DARK_TEXT); // Alerte en rouge
                addPrestigeCell(table, String.valueOf(seance.placesReservees()), bgColor, reservedColor,
                        TextAlignment.CENTER, fillRate > 0.3, cellBorder);

                // 6. DISPONIBLES
                addPrestigeCell(table, String.valueOf(seance.placesDisponibles()), bgColor, DARK_TEXT,
                        TextAlignment.CENTER, false, cellBorder);

                // 7. PRIX (Or gras)
                addPrestigeCell(table, String.format("%.2f DHS", seance.prixTicket()), bgColor, GOLD,
                        TextAlignment.RIGHT, true, cellBorder);

                isLight = !isLight;
            }

            contentCell.add(table);

            // =================================================================
            // 4. PIED DE PAGE
            // =================================================================

            // Ligne de séparation GRANDE
            contentCell.add(new Table(1).setWidth(UnitValue.createPercentValue(100))
                    .setBorder(new SolidBorder(DEEP_RED, 2.5f)).setHeight(3).setMarginTop(30)
                    .setMarginBottom(10));

            // Mention légale sobre
            Paragraph footer = new Paragraph(
                    "Document confidentiel • Propriété Commerciale : Ne pas diffuser.")
                    .setFont(PdfFontFactory.createFont(StandardFonts.TIMES_ITALIC))
                    .setFontSize(8)
                    .setFontColor(MID_GRAY)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(5);
            contentCell.add(footer);

            // Signature LUXE en Or
            Paragraph signature = new Paragraph("★ CINÉMANA ★")
                    .setFontSize(10)
                    .setFontColor(GOLD)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setBold();
            contentCell.add(signature);

            mainContainer.addCell(contentCell);
            document.add(mainContainer);

            document.close();
            logger.info("Export PDF Fiche Cinéma Prestige (Fond Clair) terminé avec succès");
            return baos.toByteArray();

        } catch (Exception e) {
            logger.error("Erreur lors de l'export PDF: {}", e.getMessage(), e);
            throw new RuntimeException("Erreur lors de la génération du PDF", e);
        }
    }

    /** Cellule d'en-tête Prestige */
    private void addPrestigeHeader(Table table, String headerText) throws IOException {
        // Fond rouge foncé, texte blanc pour un maximum d'impact
        Color headerBg = DEEP_RED;
        Color headerTextColor = PAPER_WHITE;

        Cell header = new Cell()
                .add(new Paragraph(headerText)
                        .setFont(PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD))
                        .setFontSize(10)
                        .setFontColor(headerTextColor))
                .setBackgroundColor(headerBg)
                .setTextAlignment(TextAlignment.CENTER)
                .setVerticalAlignment(VerticalAlignment.MIDDLE)
                .setPaddingTop(10)
                .setPaddingBottom(10)
                .setBorder(new SolidBorder(PAPER_WHITE, 0.5f)); // Séparateur blanc entre les colonnes
        table.addHeaderCell(header);
    }

    /** Cellule de données Prestige */
    private void addPrestigeCell(Table table, String text, Color bgColor, Color textColor,
                                 TextAlignment alignment, boolean isBold, Border bottomBorder) throws IOException {
        Paragraph p = new Paragraph(text)
                .setFontSize(9)
                .setFontColor(textColor);

        if (isBold) {
            p.setFont(PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD));
        } else {
            p.setFont(PdfFontFactory.createFont(StandardFonts.HELVETICA));
        }

        Cell cell = new Cell()
                .add(p)
                .setBackgroundColor(bgColor)
                .setTextAlignment(alignment)
                .setVerticalAlignment(VerticalAlignment.MIDDLE)
                .setPadding(8)
                .setBorder(Border.NO_BORDER)
                .setBorderBottom(bottomBorder); // Bordure inférieure grise/foncée

        table.addCell(cell);
    }

    /** Cellule Film/Genre Prestige */
    private void addPrestigeFilmCell(Table table, String titre, String genre, Color bgColor, Color titreColor,
                                     Color genreColor, TextAlignment alignment, Border bottomBorder) throws IOException {
        Paragraph pTitre = new Paragraph(titre)
                .setFont(PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD))
                .setFontSize(10)
                .setFontColor(titreColor)
                .setMarginBottom(1);

        Paragraph pGenre = new Paragraph(genre != null ? genre.toUpperCase() : "")
                .setFont(PdfFontFactory.createFont(StandardFonts.HELVETICA_OBLIQUE))
                .setFontSize(8)
                .setFontColor(genreColor)
                .setMarginTop(0);

        Cell cell = new Cell()
                .add(pTitre)
                .add(pGenre)
                .setBackgroundColor(bgColor)
                .setTextAlignment(alignment)
                .setVerticalAlignment(VerticalAlignment.MIDDLE)
                .setPadding(8)
                .setBorder(Border.NO_BORDER)
                .setBorderBottom(bottomBorder);

        table.addCell(cell);
    }

    /** Cellule Date & Heure Prestige */
    private void addPrestigeDateTimeCell(Table table, LocalDateTime dateTime, Color bgColor, Color dateColor,
                                         Color timeColor, TextAlignment alignment, Border bottomBorder) throws IOException {
        String dateStr = (dateTime != null) ? dateTime.format(DATE_ONLY_FORMATTER) : "--/--/--";
        String timeStr = (dateTime != null) ? dateTime.format(TIME_ONLY_FORMATTER) : "--:--";

        Paragraph date = new Paragraph(dateStr)
                .setFont(PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD))
                .setFontSize(10)
                .setFontColor(dateColor)
                .setMarginBottom(0);

        Paragraph time = new Paragraph(timeStr)
                .setFont(PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD))
                .setFontSize(11)
                .setFontColor(timeColor) // L'heure reste en rouge vif (CRIMSON) pour l'accentuation
                .setMarginTop(0);

        Cell cell = new Cell()
                .add(date)
                .add(time)
                .setBackgroundColor(bgColor)
                .setTextAlignment(alignment)
                .setVerticalAlignment(VerticalAlignment.MIDDLE)
                .setPadding(8)
                .setBorder(Border.NO_BORDER)
                .setBorderBottom(bottomBorder);

        table.addCell(cell);
    }
}
