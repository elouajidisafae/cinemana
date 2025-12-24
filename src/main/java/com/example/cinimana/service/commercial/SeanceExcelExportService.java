package com.example.cinimana.service.commercial;

import com.example.cinimana.dto.commercial.response.SeanceResponseDTO;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFFont;

@Service
@RequiredArgsConstructor
public class SeanceExcelExportService {

    private static final Logger logger = LoggerFactory.getLogger(SeanceExcelExportService.class);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public byte[] exportSeancesToExcel(List<SeanceResponseDTO> seances) {
        logger.info("Export Excel Prestige de {} séances", seances.size());

        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            Sheet sheet = workbook.createSheet("Séances");

            // --- Couleurs Custom (Premium) ---
            byte[] deepRedRgb = { (byte) 139, (byte) 0, (byte) 0 };
            byte[] goldRgb = { (byte) 255, (byte) 180, (byte) 0 };
            byte[] lightBgRgb = { (byte) 245, (byte) 245, (byte) 245 };

            XSSFColor deepRed = new XSSFColor(deepRedRgb, null);
            XSSFColor gold = new XSSFColor(goldRgb, null);
            XSSFColor lightBg = new XSSFColor(lightBgRgb, null);

            // --- Styles ---

            // 1. Title Style (Brand Header)
            CellStyle titleStyle = workbook.createCellStyle();
            XSSFFont titleFont = workbook.createFont();
            titleFont.setBold(true);
            titleFont.setFontHeightInPoints((short) 20);
            titleFont.setColor(deepRed);
            titleStyle.setFont(titleFont);
            titleStyle.setAlignment(HorizontalAlignment.CENTER);

            // 2. Header Style
            CellStyle headerStyle = workbook.createCellStyle();
            XSSFFont headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setColor(IndexedColors.WHITE.getIndex());
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(deepRed);
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);
            headerStyle.setBorderBottom(BorderStyle.THIN);
            headerStyle.setBorderTop(BorderStyle.THIN);
            headerStyle.setBorderLeft(BorderStyle.THIN);
            headerStyle.setBorderRight(BorderStyle.THIN);

            // 3. Data Styles (Alternating)
            CellStyle cellStyleNormal = workbook.createCellStyle();
            cellStyleNormal.setBorderBottom(BorderStyle.HAIR);

            CellStyle cellStyleAlt = workbook.createCellStyle();
            cellStyleAlt.setFillForegroundColor(lightBg);
            cellStyleAlt.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            cellStyleAlt.setBorderBottom(BorderStyle.HAIR);

            XSSFFont priceFont = workbook.createFont();
            priceFont.setColor(gold);
            priceFont.setBold(true);

            // --- Create Content ---

            // Brand Header Rows
            Row titleRow = sheet.createRow(0);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue("C I N E M A N A");
            titleCell.setCellStyle(titleStyle);
            sheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(0, 0, 0, 9));

            Row subtitleRow = sheet.createRow(1);
            Cell subtitleCell = subtitleRow.createCell(0);
            subtitleCell.setCellValue("PROGRAMME COMMERCIAL - GÉNÉRÉ LE " + LocalDateTime.now().format(DATE_FORMATTER));
            sheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(1, 1, 0, 9));

            // Space
            int rowNum = 3;

            // Create Header Row
            Row headerRow = sheet.createRow(rowNum++);
            String[] headers = { "ID", "Date & Heure", "Film", "Genre", "Salle", "Catégorie", "Prix (MAD)", "Réservées",
                    "Disponibles", "Statut" };
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // Fill Data
            boolean isLight = true;
            for (SeanceResponseDTO seance : seances) {
                Row row = sheet.createRow(rowNum++);
                CellStyle currentStyle = isLight ? cellStyleNormal : cellStyleAlt;

                createStyledCell(row, 0, String.valueOf(seance.id()), currentStyle);
                String dateStr = (seance.dateHeure() != null) ? seance.dateHeure().format(DATE_FORMATTER) : "N/A";
                createStyledCell(row, 1, dateStr, currentStyle);
                createStyledCell(row, 2, seance.filmTitre(), currentStyle);
                createStyledCell(row, 3, seance.filmGenre(), currentStyle);
                createStyledCell(row, 4, seance.salleNom(), currentStyle);
                createStyledCell(row, 5, seance.categorieNom(), currentStyle);

                Cell pCell = row.createCell(6);
                pCell.setCellValue(seance.prixTicket());
                CellStyle pStyle = workbook.createCellStyle();
                pStyle.cloneStyleFrom(currentStyle);
                pStyle.setFont(priceFont);
                pStyle.setDataFormat(workbook.createDataFormat().getFormat("#,##0.00 \"DHS\""));
                pCell.setCellStyle(pStyle);

                createStyledCell(row, 7, String.valueOf(seance.placesReservees()), currentStyle);
                createStyledCell(row, 8, String.valueOf(seance.placesDisponibles()), currentStyle);
                createStyledCell(row, 9, seance.active() ? "Active" : "Inactive", currentStyle);

                isLight = !isLight;
            }

            // Auto-size columns
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(baos);
            logger.info("Export Excel Prestige terminé avec succès");
            return baos.toByteArray();

        } catch (IOException e) {
            logger.error("Erreur lors de l'export Excel: {}", e.getMessage(), e);
            throw new RuntimeException("Erreur lors de la génération du fichier Excel", e);
        }
    }

    private void createStyledCell(Row row, int col, String value, CellStyle style) {
        Cell cell = row.createCell(col);
        cell.setCellValue(value);
        cell.setCellStyle(style);
    }
}
