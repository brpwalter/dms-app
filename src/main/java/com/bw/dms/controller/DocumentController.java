package com.bw.dms.controller;

import com.bw.dms.model.Document;
import com.bw.dms.service.StorageService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/documents")
public class DocumentController {

    private final StorageService storageService;

    public DocumentController(StorageService storageService) {
        this.storageService = storageService;
    }

    // 1. Datei hochladen
    @PostMapping("/upload")
    public ResponseEntity<?> uploadDocument(
            @RequestParam("file") MultipartFile file,
            @RequestParam("title") String title) {
        try {
            Document savedDoc = storageService.storeFile(file, title);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedDoc);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Fehler beim Hochladen der Datei: " + e.getMessage());
        }
    }

    // 2. Alle Dokumente (Metadaten) auflisten
    @GetMapping
    public ResponseEntity<List<Document>> listAllDocuments() {
        return ResponseEntity.ok(storageService.getAllDocuments());
    }

    @GetMapping("/download/{id}")
public ResponseEntity<org.springframework.core.io.Resource> downloadDocument(@PathVariable Long id) {
    org.springframework.core.io.Resource fileResource = storageService.loadFileAsResource(id);
    
    // Wir holen uns die Metadaten, um den originalen Dateinamen im Header mitzusenden
    com.bw.dms.model.Document doc = storageService.getAllDocuments().stream()
            .filter(d -> d.getId().equals(id))
            .findFirst()
            .orElseThrow();

    return ResponseEntity.ok()
            .header(org.springframework.http.HttpHeaders.CONTENT_DISPOSITION, 
                    "attachment; filename=\"" + doc.getFileName() + "\"")
            .body(fileResource);
}

}
