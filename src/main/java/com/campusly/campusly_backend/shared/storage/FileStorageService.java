package com.campusly.campusly_backend.shared.storage;

import com.campusly.campusly_backend.shared.exception.ExceptionBackend;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileStorageService {

    private final FileStorageProperties properties;

    @PostConstruct
    public void init() {
        try {
            Files.createDirectories(Paths.get(properties.getUploadDir()));
        } catch (IOException e) {
            throw new RuntimeException("Impossibile inizializzare la directory di upload: " + properties.getUploadDir(), e);
        }
    }

    public String saveFile(MultipartFile file, UUID userId) {
        validateFile(file);

        String ext = extractExtension(file.getOriginalFilename());
        String relativePath = userId + "/" + UUID.randomUUID() + "." + ext;
        Path targetPath = resolveAndValidate(relativePath);

        try {
            Files.createDirectories(targetPath.getParent());
            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            log.error("Errore salvataggio file: {}", relativePath, e);
            throw ExceptionBackend.fromError(
                    "Errore upload",
                    "Impossibile salvare il file. CODICE: FS001",
                    null, HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return relativePath;
    }

    public void deleteFile(String relativePath) {
        try {
            Path path = resolveAndValidate(relativePath);
            Files.deleteIfExists(path);
        } catch (ExceptionBackend e) {
            log.warn("Percorso non valido in deleteFile: {}", relativePath);
        } catch (IOException e) {
            log.warn("Impossibile eliminare file: {}", relativePath);
        }
    }

    public Resource loadAsResource(String relativePath) {
        Path filePath = resolveAndValidate(relativePath);

        try {
            Resource resource = new UrlResource(filePath.toUri());
            if (!resource.exists() || !resource.isReadable()) {
                throw ExceptionBackend.fromError(
                        "File non trovato",
                        "Il file richiesto non è disponibile. CODICE: FS002",
                        null, HttpStatus.NOT_FOUND);
            }
            return resource;
        } catch (ExceptionBackend e) {
            throw e;
        } catch (Exception e) {
            throw ExceptionBackend.fromError(
                    "Errore file",
                    "Impossibile recuperare il file. CODICE: FS004",
                    null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // ========================================
    //            Metodi privati
    // ========================================

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw ExceptionBackend.fromError(
                    "File non valido",
                    "Il file è vuoto. CODICE: FS010",
                    null, HttpStatus.BAD_REQUEST);
        }
        if (file.getSize() > properties.getMaxFileSize()) {
            throw ExceptionBackend.fromError(
                    "File troppo grande",
                    "Dimensione massima consentita: 5MB. CODICE: FS011",
                    null, HttpStatus.BAD_REQUEST);
        }
        String contentType = file.getContentType();
        if (contentType == null || !properties.getAllowedTypes().contains(contentType)) {
            throw ExceptionBackend.fromError(
                    "Tipo file non supportato",
                    "Formati consentiti: JPEG, PNG, WebP. CODICE: FS012",
                    null, HttpStatus.BAD_REQUEST);
        }
    }

    private Path resolveAndValidate(String relativePath) {
        Path uploadDir = Paths.get(properties.getUploadDir()).toAbsolutePath().normalize();
        Path resolved = uploadDir.resolve(relativePath).normalize();

        if (!resolved.startsWith(uploadDir)) {
            throw ExceptionBackend.fromError(
                    "Accesso negato",
                    "Percorso file non valido. CODICE: FS003",
                    null, HttpStatus.FORBIDDEN);
        }
        return resolved;
    }

    private String extractExtension(String filename) {
        if (filename == null || !filename.contains(".")) return "bin";
        return filename.substring(filename.lastIndexOf('.') + 1).toLowerCase();
    }
}
