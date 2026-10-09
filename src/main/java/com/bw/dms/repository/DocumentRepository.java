package com.bw.dms.repository;

import com.bw.dms.model.Document;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DocumentRepository extends JpaRepository<Document, Long> {
    
     // Findet die aktuell höchste Versionsnummer für ein Dokument
    @Query("SELECT MAX(d.version) FROM Document d WHERE d.id = :parentId OR d.parentDocumentId = :parentId")
    Optional<Integer> findMaxVersionByParentId(@Param("parentId") Long parentId);
}
