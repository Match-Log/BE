package com.matchlog.be.repository;

import com.matchlog.be.domain.document.Document;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface DocumentRepository extends JpaRepository<Document, Long> {

    // [GET /api/v1/boards/{boardId}] 게시글 단건 조회 — JpaRepository 기본 제공 (선언 불필요)
    // Optional<Document> findById(Long id);

    // [GET /api/v1/boards?teamId=] 팀 전체 문서 목록 — 고정글 위, 최신순
    // 파생 쿼리명이 지나치게 길어져 @Query 사용.
    @Query("SELECT d FROM Document d WHERE d.team.id = :teamId ORDER BY d.isPinned DESC, d.createdAt DESC")
    List<Document> findByTeam_IdOrderByPinnedAndCreatedAt(@Param("teamId") Long teamId);

    // [경기 삭제 후처리] 경기에 연결된 NOTE 문서 목록 조회 (VOTE 게시글은 서비스에서 명시 삭제)
    List<Document> findByTeam_IdAndMatch_Id(Long teamId, Long matchId);
}
