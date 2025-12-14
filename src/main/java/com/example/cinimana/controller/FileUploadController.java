package com.example.cinimana.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/upload")
@Slf4j
public class FileUploadController {

    private static final String UPLOAD_DIR = "uploads/films/";
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB
    private static final String[] ALLOWED_EXTENSIONS = { "png", "jpg", "jpeg" };

    @PostMapping("/film-poster")
    public ResponseEntity<?> uploadFilmPoster(@RequestParam("file") MultipartFile file) {
        try {
            // Validation: fichier vide
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body("Le fichier est vide");
            }

            // Validation: taille du fichier
            if (file.getSize() > MAX_FILE_SIZE) {
                return ResponseEntity.badRequest().body("Le fichier est trop volumineux (max 5MB)");
            }

            // Validation: type de fichier
            String originalFilename = file.getOriginalFilename();
            if (originalFilename == null || !isValidFileExtension(originalFilename)) {
                return ResponseEntity.badRequest().body("Format de fichier non supporté. Utilisez PNG, JPG ou JPEG");
            }

            // Créer le dossier s'il n'existe pas
            File uploadDir = new File(UPLOAD_DIR);
            if (!uploadDir.exists()) {
                uploadDir.mkdirs();
            }

            // Générer un nom de fichier unique
            String fileExtension = getFileExtension(originalFilename);
            String uniqueFilename = UUID.randomUUID().toString() + "." + fileExtension;
            Path filePath = Paths.get(UPLOAD_DIR + uniqueFilename);

            // Sauvegarder le fichier
            Files.write(filePath, file.getBytes());

            // Retourner l'URL du fichier
            String fileUrl = "/uploads/films/" + uniqueFilename;
            log.info("Fichier uploadé avec succès: {}", fileUrl);

            return ResponseEntity.ok(new UploadResponse(fileUrl));

        } catch (IOException e) {
            log.error("Erreur lors de l'upload du fichier", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erreur lors de l'upload du fichier");
        }
    }

    private boolean isValidFileExtension(String filename) {
        String extension = getFileExtension(filename).toLowerCase();
        for (String allowed : ALLOWED_EXTENSIONS) {
            if (allowed.equals(extension)) {
                return true;
            }
        }
        return false;
    }

    private String getFileExtension(String filename) {
        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex > 0 && lastDotIndex < filename.length() - 1) {
            return filename.substring(lastDotIndex + 1);
        }
        return "";
    }

    // Classe interne pour la réponse
    public record UploadResponse(String url) {
    }
}
