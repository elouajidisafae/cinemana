package com.example.cinimana.service.commercial;

import com.example.cinimana.dto.commercial.response.ReservationSimpleDTO;
import com.example.cinimana.model.StatutReservation;
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
 * Service d'export PDF - Design Fiche Cinéma Prestige (Fond Clair)
 * Adapté pour la liste des Réservations
 */
@Service
@RequiredArgsConstructor
public class ReservationPdfExportService {

    private static final Logger logger = LoggerFactory.getLogger(ReservationPdfExportService.class);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final DateTimeFormatter DATE_ONLY_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yy");
    private static final DateTimeFormatter TIME_ONLY_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    // Palette de couleurs (Identique à SeancePdfExportService)
    private static final Color DEEP_RED = new DeviceRgb(139, 0, 0); // #8B0000
    private static final Color CRIMSON = new DeviceRgb(220, 20, 60); // #DC143C
    private static final Color GOLD = new DeviceRgb(255, 180, 0); // #FFB400
    private static final Color PAPER_WHITE = new DeviceRgb(255, 255, 255);
    private static final Color DARK_TEXT = new DeviceRgb(30, 30, 30);
    private static final Color MID_GRAY = new DeviceRgb(100, 100, 100);
    private static final Color LIGHT_BG = new DeviceRgb(245, 245, 245);
    private static final Color VALIDATED_GREEN = new DeviceRgb(34, 139, 34); // Vert pour validé

    public byte[] exportReservationsToPdf(List<ReservationSimpleDTO> reservations) {
        logger.info("Export PDF Liste Réservations Prestige (Fond Clair) de {} réservations",
                reservations.size());

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            document.setMargins(40, 40, 40, 40);
            document.setBackgroundColor(PAPER_WHITE);

            // Conteneur principal avec bordure rouge
            Table mainContainer = new Table(1);
            mainContainer.setWidth(UnitValue.createPercentValue(100));
            mainContainer.setBackgroundColor(PAPER_WHITE);
            mainContainer.setBorder(new SolidBorder(DEEP_RED, 2));

            Cell contentCell = new Cell();
            contentCell.setBorder(Border.NO_BORDER);
            contentCell.setPadding(30);
            contentCell.setBackgroundColor(PAPER_WHITE);

            // =================================================================
            // 1. EN-TÊTE PRESTIGE
            // =================================================================

            Paragraph mainTitle = new Paragraph("C I N E M A N A")
                    .setFont(PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD))
                    .setFontSize(28)
                    .setFontColor(DEEP_RED)
                    .setTextAlignment(TextAlignment.LEFT)
                    .setMarginTop(0)
                    .setMarginBottom(5);
            contentCell.add(mainTitle);

            Paragraph subtitle = new Paragraph("LISTE DES RÉSERVATIONS")
                    .setFont(PdfFontFactory.createFont(StandardFonts.TIMES_ITALIC))
                    .setFontSize(12)
                    .setFontColor(DARK_TEXT)
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

            // Première paire : GÉNÉRÉ LE
            addInfoPair(infoTable, "GÉNÉRÉ LE :", LocalDateTime.now().format(DATE_FORMATTER), MID_GRAY,
                    DARK_TEXT,
                    TextAlignment.LEFT);
            // Deuxième paire : TOTAL
            addInfoPair(infoTable, "TOTAL RÉS. :", String.valueOf(reservations.size()), MID_GRAY, DARK_TEXT,
                    TextAlignment.RIGHT);

            contentCell.add(infoTable);

            // =================================================================
            // 3. TABLEAU PRINCIPAL
            // =================================================================

            float[] columnWidths = { 2f, 3f, 3.5f, 1.5f, 2f }; // Ajustement des largeurs
            Table table = new Table(UnitValue.createPercentArray(columnWidths));
            table.setWidth(UnitValue.createPercentValue(100));
            table.setMarginTop(10);
            table.setBorder(Border.NO_BORDER);

            // En-têtes
            addPrestigeHeader(table, "DATE RÉSA.");
            addPrestigeHeader(table, "CLIENT");
            addPrestigeHeader(table, "FILM / SÉANCE");
            addPrestigeHeader(table, "MONTANT");
            addPrestigeHeader(table, "STATUT");

            // Données
            boolean isLight = true;
            for (ReservationSimpleDTO res : reservations) {
                Color bgColor = isLight ? PAPER_WHITE : LIGHT_BG;
                Border cellBorder = new SolidBorder(MID_GRAY, 0.25f);

                // 1. DATE RÉSA
                addPrestigeDateTimeCell(table, res.dateReservation(), bgColor, DARK_TEXT, MID_GRAY,
                        TextAlignment.LEFT,
                        cellBorder);

                // 2. CLIENT
                addPrestigeCell(table, res.clientNom() + " " + res.clientPrenom(), bgColor, DARK_TEXT,
                        TextAlignment.LEFT, true, cellBorder);

                // 3. FILM / SÉANCE
                addPrestigeFilmSeanceCell(table, res.filmTitre(), res.seanceDateHeure(), bgColor,
                        DARK_TEXT, CRIMSON,
                        TextAlignment.LEFT, cellBorder);

                // 4. MONTANT
                addPrestigeCell(table, String.format("%.2f DHS", res.montantTotal()), bgColor, GOLD,
                        TextAlignment.RIGHT, true, cellBorder);

                // 5. STATUT
                Color statutColor = getStatutColor(res.statut());
                addPrestigeCell(table, res.statut().toString(), bgColor, statutColor,
                        TextAlignment.CENTER, true,
                        cellBorder);

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

            // Mention légale
            Paragraph footer = new Paragraph(
                    "Document confidentiel • Propriété Commerciale : Ne pas diffuser.")
                    .setFont(PdfFontFactory.createFont(StandardFonts.TIMES_ITALIC))
                    .setFontSize(8)
                    .setFontColor(MID_GRAY)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(5);
            contentCell.add(footer);

            // Signature
            Paragraph signature = new Paragraph("★ CINÉMANA ★")
                    .setFontSize(10)
                    .setFontColor(GOLD)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setBold();
            contentCell.add(signature);

            mainContainer.addCell(contentCell);
            document.add(mainContainer);

            document.close();
            return baos.toByteArray();

        } catch (Exception e) {
            logger.error("Erreur lors de l'export PDF Réservations: {}", e.getMessage(), e);
            throw new RuntimeException("Erreur lors de la génération du PDF", e);
        }
    }

    private Color getStatutColor(StatutReservation statut) {
        switch (statut) {
            case VALIDEE:
                return VALIDATED_GREEN;
            case ANNULEE:
                return DEEP_RED;
            case EN_ATTENTE:
                return GOLD;
            default:
                return MID_GRAY;
        }
    }

    // --- Helpers (Similaires à SeancePdfExportService) ---

    private void addPrestigeHeader(Table table, String headerText) throws IOException {
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
                .setBorder(new SolidBorder(PAPER_WHITE, 0.5f));
        table.addHeaderCell(header);
    }

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
                .setBorderBottom(bottomBorder);

        table.addCell(cell);
    }

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
                .setFont(PdfFontFactory.createFont(StandardFonts.HELVETICA))
                .setFontSize(9)
                .setFontColor(timeColor)
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

    private void addPrestigeFilmSeanceCell(Table table, String filmTitre, LocalDateTime seanceDate, Color bgColor,
                                           Color titreColor, Color dateColor, TextAlignment alignment, Border bottomBorder)
            throws IOException {
        Paragraph pTitre = new Paragraph(filmTitre)
                .setFont(PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD))
                .setFontSize(10)
                .setFontColor(titreColor)
                .setMarginBottom(1);

        String seanceDateStr = (seanceDate != null) ? seanceDate.format(DATE_FORMATTER) : "N/A";
        Paragraph pDate = new Paragraph("Séance : " + seanceDateStr)
                .setFont(PdfFontFactory.createFont(StandardFonts.HELVETICA_OBLIQUE))
                .setFontSize(8)
                .setFontColor(dateColor)
                .setMarginTop(0);

        Cell cell = new Cell()
                .add(pTitre)
                .add(pDate)
                .setBackgroundColor(bgColor)
                .setTextAlignment(alignment)
                .setVerticalAlignment(VerticalAlignment.MIDDLE)
                .setPadding(8)
                .setBorder(Border.NO_BORDER)
                .setBorderBottom(bottomBorder);

        table.addCell(cell);
    }

    private void addInfoPair(Table infoTable, String key, String value, Color keyColor, Color valueColor,
                             TextAlignment alignment) throws IOException {
        Color bg = PAPER_WHITE;

        Paragraph p = new Paragraph()
                .add(new Text(key).setFont(PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD))
                        .setFontSize(9)
                        .setFontColor(keyColor))
                .add(new Text(" " + value).setFont(PdfFontFactory.createFont(StandardFonts.HELVETICA))
                        .setFontSize(9)
                        .setFontColor(valueColor));

        Cell cell = new Cell()
                .add(p)
                .setBackgroundColor(bg)
                .setTextAlignment(alignment)
                .setPadding(0)
                .setBorder(Border.NO_BORDER);

        infoTable.addCell(cell);
    }
}
