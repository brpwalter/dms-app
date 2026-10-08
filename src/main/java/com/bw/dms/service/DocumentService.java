package com.bw.dms.service;

import com.bw.dms.model.Document;
import com.bw.dms.model.DocumentVersion;
import com.bw.dms.model.User;
import com.bw.dms.repository.DocumentRepository;

import org.apache.tika.Tika;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class DocumentService {
    private final DocumentRepository docRepo;
    private final Tika tika = new Tika();

    DocumentService(DocumentRepository docRepo) {
        this.docRepo = docRepo;
    }

    @Transactional
    public Document addVersion(Long documentId, MultipartFile file, User user) throws IOException {
        Document doc = docRepo.findById(documentId).orElseThrow();
        List<DocumentVersion> versions = getVersions(doc);
        int nextVersionNumber = versions.size() + 1;

        // Datei auf Server-Dateisystem sichern
        String uniqueName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
        Path path = Paths.get("./dms-storage").resolve(uniqueName);
        Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);

        // Volltext extrahieren
        String extractedText = "";
        try {
            extractedText = tika.parseToString(file.getInputStream());
        } catch (Exception e) {
            extractedText = "Text nicht extrahierbar.";
        }

        DocumentVersion version = new DocumentVersion();
        version.setDocument(doc);
        version.setVersionNumber(nextVersionNumber);
        version.setFileName(file.getOriginalFilename());
        version.setStoragePath(path.toString());
        version.setExtractedText(extractedText);
        version.setUploadedAt(LocalDateTime.now());

        versions.add(version);
        return docRepo.save(doc);
    }

    private List<DocumentVersion> getVersions(Document doc) {
        for (String getterName : List.of("getVersions", "getDocumentVersions")) {
            try {
                Method method = doc.getClass().getMethod(getterName);
                @SuppressWarnings("unchecked")
                List<DocumentVersion> versions = (List<DocumentVersion>) method.invoke(doc);
                if (versions != null) {
                    return versions;
                }
            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                // Ignore and fall back to the next getter name.
            }
        }
        throw new IllegalStateException("Document does not expose a version collection.");
    }
}

