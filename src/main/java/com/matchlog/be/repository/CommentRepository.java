package com.matchlog.be.repository;

import com.matchlog.be.domain.comment.Comment;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    // [GET /api/v1/boards/{boardId}] 게시글 단건 조회 시 댓글 목록 — player → user fetch join, 오래된 순
    @Query(
            "SELECT c FROM Comment c JOIN FETCH c.player p JOIN FETCH p.user WHERE c.document.id = :documentId ORDER BY c.createdAt ASC")
    List<Comment> findByDocument_IdWithPlayer(@Param("documentId") Long documentId);
}
