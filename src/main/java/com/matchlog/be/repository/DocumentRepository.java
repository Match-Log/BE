package com.matchlog.be.repository;

import com.matchlog.be.domain.document.Document;
import com.matchlog.be.constant.document.DocumentType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DocumentRepository extends JpaRepository<Document, Long> {

    List<Document> findByTeam_Id(Long teamId);

    List<Document> findByTeam_IdAndMatch_Id(Long teamId, Long matchId);

    List<Document> findByTeam_IdAndDocumentType(Long teamId, DocumentType documentType);
}
