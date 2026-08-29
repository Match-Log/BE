package com.matchlog.be.repository;

import com.matchlog.be.domain.document.Document;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DocumentRepository extends JpaRepository<Document, Long> {

    // [GET /api/v1/boards/{boardId}] 단건 조회 — player → user fetch join
    @Query("SELECT d FROM Document d JOIN FETCH d.player p JOIN FETCH p.user WHERE d.id = :id")
    Optional<Document> findByIdWithPlayer(@Param("id") Long id);

    // [GET /api/v1/boards?teamId=] 팀 게시글 목록 — 고정글 위, 최신순, player → user fetch join
    @Query(
            "SELECT d FROM Document d JOIN FETCH d.player p JOIN FETCH p.user WHERE d.team.id = :teamId ORDER BY d.isPinned DESC, d.createdAt DESC")
    List<Document> findByTeam_IdOrderByPinnedAndCreatedAt(@Param("teamId") Long teamId);
}
