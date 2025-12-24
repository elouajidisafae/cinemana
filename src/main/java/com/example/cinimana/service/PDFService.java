package com.example.cinimana.service;

import com.example.cinimana.model.Reservation;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.*;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PDFService {

    private final QRCodeService qrCodeService;

    private static final DeviceRgb RED_COLOR = new DeviceRgb(220, 38, 38); // Rouge Cinémana
    private static final DeviceRgb DARK_RED = new DeviceRgb(153, 27, 27);
    private static final DeviceRgb ZINC_900 = new DeviceRgb(24, 24, 27);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd MMMM yyyy");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    /**
     * Génère un PDF de billet de réservation "Premium Edition"
     */
    public byte[] generateReservationTicket(Reservation reservation) {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            PdfWriter writer = new PdfWriter(outputStream);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            document.setMargins(0, 0, 0, 0); // Utilise des paddings internes pour le design full-width

            // === FOND DU BILLET (SIMULATION) ===
            // On utilise un tableau principal pour tout englober si besoin,
            // mais ici on va juste structurer par sections.

            // === EN-TÊTE PREMIUM (BANDEAU ROUGE) ===
            addPremiumHeader(document);

            // === CONTENU PRINCIPAL (PADDINGS) ===
            Div mainContent = new Div()
                    .setPaddingTop(30)
                    .setPaddingRight(50)
                    .setPaddingBottom(20)
                    .setPaddingLeft(50);

            // === SECTION FILM ===
            addPremiumFilmSection(mainContent, reservation);

            // === GRILLE DE DÉTAILS ===
            addPremiumDetailsGrid(mainContent, reservation);

            // === SIÈGES & PRIX ===
            addPremiumSeatsAndPrice(mainContent, reservation);

            document.add(mainContent);

            // === LIGNE DE DÉCOUPE (Tear-off line) ===
            addPremiumTearOffLine(document);

            // === SECTION QR CODE & VALIDATION ===
            Div validationSection = new Div()
                    .setPaddingTop(20)
                    .setPaddingRight(50)
                    .setPaddingBottom(40)
                    .setPaddingLeft(50);
            addPremiumValidationSection(validationSection, reservation);
            document.add(validationSection);

            // === PIED DE PAGE ===
            addPremiumFooter(document, reservation);

            document.close();
            return outputStream.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de la génération du ticket Premium: " + e.getMessage(), e);
        }
    }

    private void addPremiumHeader(Document document) {
        Table headerTable = new Table(UnitValue.createPercentArray(new float[] { 1 }))
                .setWidth(UnitValue.createPercentValue(100))
                .setMarginLeft(0)
                .setMarginRight(0)
                .setMarginTop(0)
                .setPadding(0);

        headerTable.addCell(new Cell()
                .add(new Paragraph("CINÉMANA")
                        .setFontSize(24)
                        .setBold()
                        .setFontColor(ColorConstants.WHITE)
                        .setMarginBottom(0)
                        .setTextAlignment(TextAlignment.CENTER))
                .add(new Paragraph("L'EXPÉRIENCE ULTIME DU CINÉMA")
                        .setFontSize(7)
                        .setBold()
                        .setCharacterSpacing(3)
                        .setFontColor(new DeviceRgb(254, 202, 202)) // Rouge très clair
                        .setTextAlignment(TextAlignment.CENTER))
                .setBackgroundColor(RED_COLOR) // Fond sur la cellule pour garantir le remplissage
                .setBorder(Border.NO_BORDER)
                .setPaddingTop(30)
                .setPaddingBottom(30)
                .setPaddingLeft(20)
                .setPaddingRight(20));

        document.add(headerTable);
    }

    private void addPremiumFilmSection(Div container, Reservation reservation) {
        container.add(new Paragraph("SÉANCE RÉSERVÉE")
                .setFontSize(8)
                .setBold()
                .setFontColor(RED_COLOR)
                .setCharacterSpacing(2)
                .setMarginBottom(5));

        container.add(new Paragraph(reservation.getSeance().getFilm().getTitre().toUpperCase())
                .setFontSize(30)
                .setBold()
                .setFontColor(ZINC_900)
                .setMarginBottom(5)
                .setFixedLeading(28));

        String filmSpecs = String.format("%s  •  %d MINUTES  •  %s",
                reservation.getSeance().getFilm().getGenre().toUpperCase(),
                reservation.getSeance().getFilm().getDuree(),
                reservation.getSeance().getFilm().getAgeLimite() != null
                        ? "AGE: " + reservation.getSeance().getFilm().getAgeLimite()
                        : "TOUT PUBLIC");

        container.add(new Paragraph(filmSpecs)
                .setFontSize(9)
                .setBold()
                .setFontColor(ColorConstants.GRAY)
                .setMarginBottom(30));
    }

    private void addPremiumDetailsGrid(Div container, Reservation reservation) {
        Table grid = new Table(UnitValue.createPercentArray(new float[] { 1, 1, 1, 1 }))
                .setWidth(UnitValue.createPercentValue(100))
                .setMarginBottom(30);

        String date = reservation.getSeance().getDateHeure().format(DATE_FORMATTER);
        String heure = reservation.getSeance().getDateHeure().format(TIME_FORMATTER);
        String salle = reservation.getSeance().getSalle().getNom();
        String cat = reservation.getSeance().getCategorie() != null ? reservation.getSeance().getCategorie().getNom()
                : "STANDARD";

        grid.addCell(createPremiumGridCell("DATE", date.toUpperCase()));
        grid.addCell(createPremiumGridCell("HEURE", heure));
        grid.addCell(createPremiumGridCell("SALLE", salle.toUpperCase()));
        grid.addCell(createPremiumGridCell("EXPÉRIENCE", cat.toUpperCase()));

        container.add(grid);
    }

    private void addPremiumSeatsAndPrice(Div container, Reservation reservation) {
        Table table = new Table(UnitValue.createPercentArray(new float[] { 2, 1 }))
                .setWidth(UnitValue.createPercentValue(100));

        // Sièges
        String seatsList = reservation.getSieges().stream()
                .map(s -> String.format("R%d-S%d", s.getRangee(), s.getNumero()))
                .collect(Collectors.joining(", "));

        Cell seatsCell = new Cell()
                .add(new Paragraph("EMPLACEMENTS RÉSERVÉS")
                        .setFontSize(8)
                        .setBold()
                        .setFontColor(ColorConstants.GRAY)
                        .setCharacterSpacing(1))
                .add(new Paragraph(seatsList)
                        .setFontSize(16)
                        .setBold()
                        .setFontColor(ZINC_900))
                .setBorder(Border.NO_BORDER);

        // Total
        Cell priceCell = new Cell()
                .add(new Paragraph("TOTAL RÉGLÉ")
                        .setFontSize(8)
                        .setBold()
                        .setFontColor(ColorConstants.GRAY)
                        .setTextAlignment(TextAlignment.RIGHT))
                .add(new Paragraph(String.format("%.2f MAD", reservation.getMontantTotal()))
                        .setFontSize(22)
                        .setBold()
                        .setFontColor(RED_COLOR)
                        .setTextAlignment(TextAlignment.RIGHT))
                .setBorder(Border.NO_BORDER);

        table.addCell(seatsCell);
        table.addCell(priceCell);
        container.add(table);
    }

    private void addPremiumTearOffLine(Document document) {
        Table lineTable = new Table(UnitValue.createPercentArray(new float[] { 1 }))
                .setWidth(UnitValue.createPercentValue(100))
                .setMarginTop(10)
                .setMarginBottom(10);

        Cell lineCell = new Cell()
                .setBorder(Border.NO_BORDER)
                .setBorderTop(new com.itextpdf.layout.borders.DashedBorder(ColorConstants.GRAY, 1))
                .setHeight(1);

        lineTable.addCell(lineCell);
        document.add(lineTable);

        document.add(new Paragraph("✂")
                .setFontSize(12)
                .setFontColor(ColorConstants.GRAY)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginTop(-18)
                .setMarginBottom(10));
    }

    private void addPremiumValidationSection(Div container, Reservation reservation) {
        Table validationTable = new Table(UnitValue.createPercentArray(new float[] { 1, 2 }))
                .setWidth(UnitValue.createPercentValue(100));

        // QR Code
        try {
            byte[] qrCodeBytes = qrCodeService.generateQRCodeBytes(reservation.getCodeReservation(), 300, 300);
            Image qrImage = new Image(ImageDataFactory.create(qrCodeBytes))
                    .setWidth(110)
                    .setHorizontalAlignment(com.itextpdf.layout.properties.HorizontalAlignment.LEFT);
            validationTable.addCell(new Cell().add(qrImage).setBorder(Border.NO_BORDER));
        } catch (Exception e) {
            validationTable.addCell(new Cell().add(new Paragraph("[ERREUR QR]")).setBorder(Border.NO_BORDER));
        }

        // Info validation
        Cell infoCell = new Cell()
                .add(new Paragraph("CODE DE RÉSERVATION")
                        .setFontSize(8)
                        .setBold()
                        .setFontColor(ColorConstants.GRAY))
                .add(new Paragraph(reservation.getCodeReservation())
                        .setFontSize(10)
                        .setBold()
                        .setFontColor(ZINC_900)
                        .setMarginBottom(15))
                .add(new Paragraph("ACCÈS SALLE")
                        .setFontSize(18)
                        .setBold()
                        .setFontColor(RED_COLOR)
                        .setFixedLeading(16))
                .add(new Paragraph(
                        "PRÉSENTEZ CE BILLET À L'ENTRÉE. LE SCAN DU QR CODE EST OBLIGATOIRE POUR VALIDER VOTRE PRÉSENCE.")
                        .setFontSize(7)
                        .setFontColor(ColorConstants.GRAY)
                        .setMarginTop(5))
                .setBorder(Border.NO_BORDER)
                .setVerticalAlignment(com.itextpdf.layout.properties.VerticalAlignment.MIDDLE)
                .setPaddingLeft(20);

        validationTable.addCell(infoCell);
        container.add(validationTable);
    }

    private void addPremiumFooter(Document document, Reservation reservation) {
        Table footerTable = new Table(UnitValue.createPercentArray(new float[] { 1 }))
                .setWidth(UnitValue.createPercentValue(100))
                .setMarginLeft(0)
                .setMarginRight(0)
                .setMarginBottom(0)
                .setPadding(0);

        String reservedAt = reservation.getDateReservation().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));

        footerTable.addCell(new Cell()
                .add(new Paragraph("© 2025 CINÉMANA  |  BILLET GÉNÉRÉ LE " + reservedAt)
                        .setFontSize(6)
                        .setFontColor(ColorConstants.LIGHT_GRAY)
                        .setTextAlignment(TextAlignment.CENTER))
                .add(new Paragraph("MERCI D'AVOIR CHOISI CINÉMANA. PROFITEZ BIEN DE VOTRE SÉANCE !")
                        .setFontSize(8)
                        .setBold()
                        .setFontColor(ColorConstants.WHITE)
                        .setTextAlignment(TextAlignment.CENTER)
                        .setMarginTop(5))
                .setBackgroundColor(ZINC_900)
                .setBorder(Border.NO_BORDER)
                .setPaddingTop(20)
                .setPaddingBottom(20)
                .setPaddingLeft(10)
                .setPaddingRight(10));

        document.add(footerTable);
    }

    private Cell createPremiumGridCell(String label, String value) {
        return new Cell()
                .add(new Paragraph(label)
                        .setFontSize(7)
                        .setBold()
                        .setFontColor(RED_COLOR)
                        .setMarginBottom(2))
                .add(new Paragraph(value)
                        .setFontSize(10)
                        .setBold()
                        .setFontColor(ZINC_900))
                .setBorder(new SolidBorder(new DeviceRgb(240, 240, 240), 1f))
                .setPaddingTop(12)
                .setPaddingBottom(12)
                .setPaddingLeft(12)
                .setPaddingRight(12)
                .setBackgroundColor(new DeviceRgb(252, 252, 252));
    }
}
