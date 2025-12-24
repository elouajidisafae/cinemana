package com.example.cinimana.service.commercial;

import com.example.cinimana.dto.commercial.response.ReservationSimpleDTO;
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
public class ReservationExcelExportService {

    private static final Logger logger = LoggerFactory.getLogger(ReservationExcelExportService.class);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public byte[] exportReservationsToExcel(List<ReservationSimpleDTO> reservations) {
        logger.info("Export Excel Prestige de {} réservations", reservations.size());

        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            Sheet sheet = workbook.createSheet("Réservations");

            // --- Couleurs Custom (Premium) ---
            byte[] deepRedRgb = { (byte) 139, (byte) 0, (byte) 0 };
            byte[] goldRgb = { (byte) 255, (byte) 180, (byte) 0 };
            byte[] lightBgRgb = { (byte) 245, (byte) 245, (byte) 245 };
            byte[] validatedGreenRgb = { (byte) 34, (byte) 139, (byte) 34 };

            XSSFColor deepRed = new XSSFColor(deepRedRgb, null);
            XSSFColor gold = new XSSFColor(goldRgb, null);
            XSSFColor lightBg = new XSSFColor(lightBgRgb, null);
            XSSFColor validatedGreen = new XSSFColor(validatedGreenRgb, null);

            // --- Styles ---

            // 1. Title Style
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
            headerStyle.setBorderLeft(BorderStyle.THIN);
            headerStyle.setBorderRight(BorderStyle.THIN);
            headerStyle.setBorderTop(BorderStyle.THIN);

            // 3. Data Styles
            CellStyle cellStyleNormal = workbook.createCellStyle();
            cellStyleNormal.setBorderBottom(BorderStyle.HAIR);

            CellStyle cellStyleAlt = workbook.createCellStyle();
            cellStyleAlt.setFillForegroundColor(lightBg);
            cellStyleAlt.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            cellStyleAlt.setBorderBottom(BorderStyle.HAIR);

            // 4. Specific Fonts for status
            XSSFFont goldFont = workbook.createFont();
            goldFont.setColor(gold);
            goldFont.setBold(true);

            XSSFFont greenFont = workbook.createFont();
            greenFont.setColor(validatedGreen);
            greenFont.setBold(true);

            XSSFFont redFont = workbook.createFont();
            redFont.setColor(deepRed);
            redFont.setBold(true);

            // --- Content ---

            // Brand Headers
            Row titleRow = sheet.createRow(0);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue("C I N E M A N A");
            titleCell.setCellStyle(titleStyle);
            sheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(0, 0, 0, 6));

            Row subtitleRow = sheet.createRow(1);
            Cell subtitleCell = subtitleRow.createCell(0);
            subtitleCell
                    .setCellValue("LISTE DES RÉSERVATIONS - GÉNÉRÉ LE " + LocalDateTime.now().format(DATE_FORMATTER));
            sheet.addMergedRegion(new org.apache.poi.ss.util.CellRangeAddress(1, 1, 0, 6));

            int rowNum = 3;

            // Create Header Row
            Row headerRow = sheet.createRow(rowNum++);
            String[] headers = { "ID", "Date de Réservation", "Client", "Film", "Date Séance", "Statut",
                    "Montant (MAD)" };
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // Fill Data
            boolean isLight = true;
            for (ReservationSimpleDTO res : reservations) {
                Row row = sheet.createRow(rowNum++);
                CellStyle currentStyle = isLight ? cellStyleNormal : cellStyleAlt;

                createCell(row, 0, String.valueOf(res.id()), currentStyle);
                String dateResaStr = (res.dateReservation() != null) ? res.dateReservation().format(DATE_FORMATTER)
                        : "N/A";
                createCell(row, 1, dateResaStr, currentStyle);
                createCell(row, 2, res.clientNom() + " " + res.clientPrenom(), currentStyle);
                createCell(row, 3, res.filmTitre(), currentStyle);
                String seanceDateStr = (res.seanceDateHeure() != null) ? res.seanceDateHeure().format(DATE_FORMATTER)
                        : "N/A";
                createCell(row, 4, seanceDateStr, currentStyle);

                // Statut with custom colorFont
                Cell statusCell = row.createCell(5);
                statusCell.setCellValue(res.statut().toString());
                CellStyle statusStyle = workbook.createCellStyle();
                statusStyle.cloneStyleFrom(currentStyle);
                statusStyle.setFont(getStatusFont(res.statut().toString(), greenFont, redFont, goldFont,
                        (XSSFFont) workbook.getFontAt(0)));
                statusCell.setCellStyle(statusStyle);

                // Montant
                Cell amountCell = row.createCell(6);
                amountCell.setCellValue(res.montantTotal());
                CellStyle mStyle = workbook.createCellStyle();
                mStyle.cloneStyleFrom(currentStyle);
                mStyle.setFont(goldFont);
                mStyle.setDataFormat(workbook.createDataFormat().getFormat("#,##0.00 \"DHS\""));
                amountCell.setCellStyle(mStyle);

                isLight = !isLight;
            }

            // Auto-size columns
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(baos);
            logger.info("Export Excel Prestige Réservations terminé avec succès");
            return baos.toByteArray();
        } catch (IOException e) {
            logger.error("Erreur export Excel réservations", e);
            throw new RuntimeException("Erreur export Excel", e);
        }
    }

    private void createCell(Row row, int col, String value, CellStyle style) {
        Cell cell = row.createCell(col);
        cell.setCellValue(value);
        cell.setCellStyle(style);
    }

    private XSSFFont getStatusFont(String status, XSSFFont green, XSSFFont red, XSSFFont gold, XSSFFont def) {
        if (status.equals("VALIDEE"))
            return green;
        if (status.equals("ANNULEE"))
            return red;
        if (status.equals("EN_ATTENTE"))
            return gold;
        return def;
    }
}
