package com.bw.dms.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "document_versions")
@Getter // Generiert alle Getter automatisch
@Setter // Generiert alle Setter automatisch
@NoArgsConstructor // Generiert den Standard-Konstruktor automatisch
public class DocumentVersion {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne @JoinColumn(name = "document_id")
    private Document document;

    private int versionNumber; // z.B. 1, 2, 3
    private String fileName;
    private String fileType;
    private Long fileSize;
    private String storagePath;

    @Column(columnDefinition = "TEXT")
    private String extractedText; // Basis für die Volltextsuche

    private LocalDateTime uploadedAt;
}
