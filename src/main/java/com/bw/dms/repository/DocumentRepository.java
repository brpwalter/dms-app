package com.bw.dms.repository;

import com.bw.dms.model.Document;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DocumentRepository extends JpaRepository<Document, Long> {
    
    @Query(value = "SELECT DISTINCT d.* FROM documents d " +
                   "JOIN document_versions dv ON d.id = dv.document_id " +
                   "WHERE (:query IS NULL OR d.title ILIKE CONCAT('%', :query, '%') " +
                   "OR dv.file_name ILIKE CONCAT('%', :query, '%') " +
                   "OR dv.extracted_text ILIKE CONCAT('%', :query, '%'))", 
           nativeQuery = true)
    List<Document> searchDocuments(@Param("query") String query);
}

