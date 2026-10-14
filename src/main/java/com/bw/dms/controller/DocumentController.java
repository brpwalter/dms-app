package com.bw.dms.controller;

import com.bw.dms.model.Document;
import com.bw.dms.repository.DocumentRepository;
import com.bw.dms.service.StorageService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/api/documents")
public class DocumentController {

    private final StorageService storageService;

    private final String uploadDirectory = "C:/dms_storage/"; 

    private final Path rootLocation = Paths.get("uploaded-files");

      private final DocumentRepository documentRepository; // Dein JPA-Repository für PostgreSQL

      @Autowired 
      private StorageService documentService;

  /*    public DocumentController() {
        this.storageService = null;
        this.documentRepository = null;
    } */

      public DocumentController(DocumentRepository documentRepository) {
        this.storageService = null;
        this.documentRepository = documentRepository;
    }

/*        public DocumentController(StorageService documentService) {
        this.documentService = documentService;
        this.storageService = null;
        this.documentRepository = null;
    }
         */

  @PostMapping
    public ResponseEntity<Document> uploadDocument(
            @RequestParam("file") MultipartFile file,
            @RequestParam("title") String title,
            @RequestParam(value = "categoryId", required = false) Long categoryId) {

        try {
            Files.createDirectories(rootLocation);
            String customFileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
            Path targetPath = this.rootLocation.resolve(customFileName);
            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

            Document doc = new Document();
            doc.setTitle(title);
            doc.setFileName(customFileName);
            doc.setFileType(file.getContentType());
            doc.setStoragePath(targetPath.toString());
            doc.setFileSize(file.getSize());
            doc.setUploadTime(LocalDateTime.now());
            doc.setVersion(1);
            doc.setCategoryId(categoryId);
                 Document savedDoc = documentRepository.save(doc);
            return ResponseEntity.ok(savedDoc);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build(); 
       }
    }
    
      // 2. VERSION UPLOAD (Neue Unterversion anhängen)
    @PostMapping("/version")
    public ResponseEntity<Document> uploadNewVersion(
            @RequestParam("file") MultipartFile file,
            @RequestParam("title") String title,
            @RequestParam("parentDocumentId") Long parentDocumentId,
            @RequestParam(value = "categoryId", required = false) Long categoryId) {
        try {
            Files.createDirectories(rootLocation);
            String customFileName = System.currentTimeMillis() + "_v_" + file.getOriginalFilename();
            Path targetPath = this.rootLocation.resolve(customFileName);
            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

            // Ermittle die nächste Versionsnummer (Max aktuelle Version + 1)
            int nextVersion = documentRepository.findMaxVersionByParentId(parentDocumentId).orElse(1) + 1;

            Document doc = new Document();
            doc.setTitle(title);
            doc.setFileName(customFileName);
            doc.setFileType(file.getContentType());
            doc.setStoragePath(targetPath.toString());
            doc.setFileSize(file.getSize());
            doc.setUploadTime(LocalDateTime.now());
            doc.setVersion(nextVersion);
            doc.setParentDocumentId(parentDocumentId);
            doc.setCategoryId(categoryId);

            Document savedDoc = documentRepository.save(doc);
            return ResponseEntity.ok(savedDoc);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

      @GetMapping("/download/{id}")
    public ResponseEntity<Resource> downloadFile(@PathVariable Long id) {
        Document doc = documentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Dokument nicht gefunden"));
        try {
            Path file = Paths.get(doc.getStoragePath());
            Resource resource = new UrlResource(file.toUri());

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + doc.getTitle() + "\"")
                    .body(resource);
        } catch (MalformedURLException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

      // 4. PREVIEW (Im Browser öffnen)
    @GetMapping("/preview/{id}")
    public ResponseEntity<Resource> previewFile(@PathVariable Long id) {
        Document doc = documentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Dokument nicht gefunden"));
        try {
            Path file = Paths.get(doc.getStoragePath());
            Resource resource = new UrlResource(file.toUri());

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(doc.getFileType()))
                    // "inline" sorgt dafür, dass es im Browser angezeigt wird (z.B. PDFs, Bilder)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + doc.getTitle() + "\"")
                    .body(resource);
        } catch (MalformedURLException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // 5. MOVE (Kategorie ändern)
    @PutMapping("/{id}/move")
    public ResponseEntity<Document> moveDocument(
            @PathVariable Long id,
            @RequestParam(value = "categoryId", required = false) Long categoryId) {
        Document doc = documentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Dokument nicht gefunden"));
        
        doc.setCategoryId(categoryId);
        Document updatedDoc = documentRepository.save(doc);
        return ResponseEntity.ok(updatedDoc);
    }

    // 6. DELETE
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDocument(@PathVariable Long id) {
        Document doc = documentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Dokument nicht gefunden"));
        try {
            // Datei von der Festplatte löschen
            Path filePath = Paths.get(doc.getStoragePath());
            Files.deleteIfExists(filePath);

            // Eintrag aus PostgreSQL löschen
            documentRepository.delete(doc);
            return ResponseEntity.noContent().build();
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

      // Beispielhafter Such-Endpunkt gegen den 404-Fehler
  @GetMapping("/search")
public ResponseEntity<List<Document>> searchDocuments(@RequestParam("q") String query) {
    if (query == null || query.trim().isEmpty()) {
        // Falls die Suche leer ist, einfach alle Dokumente zurückgeben
        return ResponseEntity.ok(documentRepository.findAll());
    }
    
    // Ruft die PostgreSQL-Volltextsuche auf, die wir in Schritt 4 gebaut haben
    List<Document> results = documentRepository.searchByContent(query);
    return ResponseEntity.ok(results);
}


    // 2. Alle Dokumente (Metadaten) auflisten
    @GetMapping
    public ResponseEntity<List<Document>> getAllDocuments() {
        List<Document> documents = documentService.getAllDocuments(); // bzw. repo.findAll()
        return ResponseEntity.ok(documents);
    }
    
 /*   @GetMapping("/download/{id}")
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
} */

            

}
