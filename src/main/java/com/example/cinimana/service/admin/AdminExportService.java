package com.example.cinimana.service.admin;

import com.example.cinimana.dto.response.HistoriqueResponseDTO;
import com.example.cinimana.model.Client;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminExportService {

    private static final Logger logger = LoggerFactory.getLogger(AdminExportService.class);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    /**
     * Export historique WITHOUT Montant column (for Users, Films, Offres, Salles,
     * Seances)
     */
    public byte[] exportHistoriqueToExcel(List<HistoriqueResponseDTO> entries) {
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream(); // Create OutputStream
            Sheet sheet = workbook.createSheet("Historique"); // Create Sheet

            // Header Style
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            headerStyle.setBorderBottom(BorderStyle.THIN);

            // Create Header Row WITHOUT Montant
            Row headerRow = sheet.createRow(0);
            String[] headers = { "ID Op", "Type Entité", "ID Entité", "Nom Entité", "Opération", "Date", "Admin",
                    "Infos" };
            for (int i = 0; i < headers.length; i++) { // Create cells
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // Fill Data WITHOUT Montant
            int rowNum = 1;
            for (HistoriqueResponseDTO entry : entries) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(entry.idOperation() != null ? entry.idOperation().toString() : "");
                row.createCell(1).setCellValue(entry.entiteType() != null ? entry.entiteType() : "");
                row.createCell(2).setCellValue(entry.entiteId() != null ? entry.entiteId() : "");
                row.createCell(3).setCellValue(entry.entiteNom() != null ? entry.entiteNom() : "");
                row.createCell(4).setCellValue(entry.operation() != null ? entry.operation() : "");
                row.createCell(5).setCellValue(
                        entry.dateOperation() != null ? entry.dateOperation().format(DATE_FORMATTER) : "");
                row.createCell(6).setCellValue(entry.adminNomComplet() != null ? entry.adminNomComplet() : "");
                row.createCell(7).setCellValue(entry.infoSupplementaire() != null ? entry.infoSupplementaire() : "");
            }

            // Auto-size columns
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(baos); // Write to OutputStream
            return baos.toByteArray();
        } catch (IOException e) {
            logger.error("Error generating Excel for history: {}", e.getMessage());
            throw new RuntimeException("Erreur lors de la génération du fichier Excel", e);
        }
    }

    /**
     * Export historique WITH Montant column (for Reservations only)
     */
    public byte[] exportReservationHistoriqueToExcel(List<HistoriqueResponseDTO> entries) {
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            Sheet sheet = workbook.createSheet("Historique");

            // Header Style
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            headerStyle.setBorderBottom(BorderStyle.THIN);

            // Create Header Row WITH Montant
            Row headerRow = sheet.createRow(0);
            String[] headers = { "ID Op", "Type Entité", "ID Entité", "Nom Entité", "Opération", "Date", "Admin",
                    "Montant", "Infos" };
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // Fill Data WITH Montant
            int rowNum = 1;
            for (HistoriqueResponseDTO entry : entries) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(entry.idOperation() != null ? entry.idOperation().toString() : "");
                row.createCell(1).setCellValue(entry.entiteType() != null ? entry.entiteType() : "");
                row.createCell(2).setCellValue(entry.entiteId() != null ? entry.entiteId() : "");
                row.createCell(3).setCellValue(entry.entiteNom() != null ? entry.entiteNom() : "");
                row.createCell(4).setCellValue(entry.operation() != null ? entry.operation() : "");
                row.createCell(5).setCellValue(
                        entry.dateOperation() != null ? entry.dateOperation().format(DATE_FORMATTER) : "");
                row.createCell(6).setCellValue(entry.adminNomComplet() != null ? entry.adminNomComplet() : "");
                row.createCell(7).setCellValue(entry.montant() != null ? entry.montant() : 0.0);
                row.createCell(8).setCellValue(entry.infoSupplementaire() != null ? entry.infoSupplementaire() : "");
            }

            // Auto-size columns
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(baos);
            return baos.toByteArray();
        } catch (IOException e) {
            logger.error("Error generating Excel for reservation history: {}", e.getMessage());
            throw new RuntimeException("Erreur lors de la génération du fichier Excel", e);
        }
    }

    public byte[] exportClientsToExcel(List<Client> clients) {
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            Sheet sheet = workbook.createSheet("Clients");

            // Header Style
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            headerStyle.setBorderBottom(BorderStyle.THIN);

            // Create Header Row
            Row headerRow = sheet.createRow(0);
            String[] headers = { "ID", "Nom", "Prénom", "Email", "Téléphone", "Date Naissance" };
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // Fill Data
            int rowNum = 1;
            for (Client client : clients) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(client.getId() != null ? client.getId().toString() : "");
                row.createCell(1).setCellValue(client.getNom() != null ? client.getNom() : "");
                row.createCell(2).setCellValue(client.getPrenom() != null ? client.getPrenom() : "");
                row.createCell(3).setCellValue(client.getEmail() != null ? client.getEmail() : "");
                row.createCell(4).setCellValue(client.getNumeroTelephone() != null ? client.getNumeroTelephone() : "");
                row.createCell(5)
                        .setCellValue(client.getDateNaissance() != null ? client.getDateNaissance().toString() : "");
            }

            // Auto-size columns
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(baos);
            return baos.toByteArray();
        } catch (IOException e) {
            logger.error("Error generating Excel for clients: {}", e.getMessage());
            throw new RuntimeException("Erreur lors de la génération du fichier Excel", e);
        }
    }
}
