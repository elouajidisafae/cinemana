package com.example.cinimana.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;

@Service
public class QRCodeService {

    /**
     * Génère un QR code à partir d'un code de réservation
     *
     * @param codeReservation Le code unique de la réservation
     * @param width           Largeur du QR code en pixels
     * @param height          Hauteur du QR code en pixels
     * @return QR code en format Base64 (pour affichage direct ou PDF)
     */
    public String generateQRCode(String codeReservation, int width, int height) {
        try {
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(
                    codeReservation,
                    BarcodeFormat.QR_CODE,
                    width,
                    height);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(bitMatrix, "PNG", outputStream);
            byte[] qrCodeBytes = outputStream.toByteArray();

            return Base64.getEncoder().encodeToString(qrCodeBytes);

        } catch (WriterException | IOException e) {
            throw new RuntimeException("Erreur lors de la génération du QR code", e);
        }
    }

    /**
     * Génère un QR code avec dimensions par défaut (300x300)
     */
    public String generateQRCode(String codeReservation) {
        return generateQRCode(codeReservation, 300, 300);
    }

    /**
     * Génère un QR code et retourne les bytes directement (pour PDF)
     */
    public byte[] generateQRCodeBytes(String codeReservation, int width, int height) {
        try {
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(
                    codeReservation,
                    BarcodeFormat.QR_CODE,
                    width,
                    height);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(bitMatrix, "PNG", outputStream);
            return outputStream.toByteArray();

        } catch (WriterException | IOException e) {
            throw new RuntimeException("Erreur lors de la génération du QR code", e);
        }
    }
}
