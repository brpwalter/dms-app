package com.bw.dms.service;

import com.bw.dms.model.Document;
import com.bw.dms.repository.DocumentRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.List;

@Service
public class StorageService {

    // Ordner, in dem die Dateien auf dem Server landen
    private final Path rootLocation = Paths.get("uploaded-files");
    private final DocumentRepository documentRepository;

    public StorageService(DocumentRepository documentRepository) {
        this.documentRepository = documentRepository;
        // Ordner erstellen, falls er nicht existiert
        try {
            Files.createDirectories(rootLocation);
        } catch (IOException e) {
            throw new RuntimeException("Konnte den Speicherordner nicht initialisieren!", e);
        }
    }

    public Document storeFile(MultipartFile file, String title) throws IOException {
        // Eindeutigen Dateinamen generieren (Zeitsstempel + Originalname)
        String uniqueFileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
        Path destinationFile = this.rootLocation.resolve(uniqueFileName);

        // Datei physisch speichern
        Files.copy(file.getInputStream(), destinationFile, StandardCopyOption.REPLACE_EXISTING);

        // Metadaten-Objekt erstellen und in PostgreSQL speichern
        Document document = new Document(
                title,
                uniqueFileName,
                file.getContentType(),
                file.getSize(),
                destinationFile.toString()
        );

        return documentRepository.save(document);
    }

    public List<Document> getAllDocuments() {
        return documentRepository.findAll();
    }

    public org.springframework.core.io.Resource loadFileAsResource(Long id) {
    try {
        // Dokument in der DB suchen
        Document document = documentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Dokument mit ID " + id + " nicht gefunden"));

        Path filePath = Paths.get(document.getStoragePath());
        org.springframework.core.io.Resource resource = new org.springframework.core.io.UrlResource(filePath.toUri());

        if (resource.exists() || resource.isReadable()) {
            return resource;
        } else {
            throw new RuntimeException("Datei konnte nicht gelesen werden: " + document.getFileName());
        }
    } catch (Exception e) {
        throw new RuntimeException("Fehler beim Laden der Datei: " + e.getMessage(), e);
    }
}

}
