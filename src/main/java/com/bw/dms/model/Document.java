package com.bw.dms.model; // Pfad anpassen

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.Date;

@Entity
@Table(name = "documents")
public class Document {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String fileType;
    private Long fileSize;

    @Column(columnDefinition = "TEXT")
private String fileContent; // Hier landet der extrahierte Text


    @SuppressWarnings("deprecation")
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt; // Das Feld für das Datum

    @Lob
    @Column(name = "file_data")
    private byte[] fileData; // Das Feld für die Datei-Bytes (BYTEA in Postgres)

    private String fileName;

      @Column(name = "storage_path", nullable = false)
    private String storagePath; // Speichert z.B. "C:/dms_storage/rechnung_123.pdf"

    private LocalDateTime uploadTime;
    
    // NEU: Versionierung (Standardmäßig 1)
    private Integer version = 1;

        @Column(name = "parent_document_id")
    private Long parentDocumentId;

    // NEU: Verknüpfung zur Kategorie
    @Column(name = "category_id")
    private Long categoryId;

        public Document() {
            title = "";
            fileType = "";
            fileSize = 0L;
            createdAt = new Date(); // Setzt das aktuelle Datum beim Erstellen    
              
    }

    // Klassischer Konstruktor
    public Document(String title2, String uniqueFileName, String string, long l, String string2) {
    }



    // MANUELLE GETTER UND SETTER (falls Lombok bei dir nicht greift)
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getFileType() { return fileType; }
    public void setFileType(String fileType) { this.fileType = fileType; }

    public Long getFileSize() { return fileSize; }
    public void setFileSize(Long fileSize) { this.fileSize = fileSize; }

    // Diese beiden Methoden fangen den Fehler im Controller ab:
    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }

    public byte[] getFileData() { return fileData; }
    public void setFileData(byte[] fileData) { this.fileData = fileData; }

    // NEU: Originaler Dateiname
    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }

   // Getter und Setter
    public String getStoragePath() {
        return this.storagePath;
    }

    public void setStoragePath(String storagePath) {
        this.storagePath = storagePath;
    }

    public LocalDateTime getUploadTime() {
        return uploadTime;
    }

    public void setUploadTime(LocalDateTime uploadTime) {
        this.uploadTime = uploadTime;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public Long getParentDocumentId() {
        return parentDocumentId;
    }

    public void setParentDocumentId(Long parentDocumentId) {
        this.parentDocumentId = parentDocumentId;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }
    
}
