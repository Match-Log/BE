package com.matchlog.be.controller.comment;

import com.matchlog.be.dto.comment.request.CreateCommentRequestDto;
import com.matchlog.be.dto.comment.request.UpdateCommentRequestDto;
import com.matchlog.be.dto.comment.response.CommentResponseDto;
import com.matchlog.be.service.comment.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @PostMapping("/boards/{boardId}/comments")
    public ResponseEntity<CommentResponseDto> createComment(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long boardId,
            @RequestBody CreateCommentRequestDto request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(commentService.createComment(userId, boardId, request));
    }

    @PatchMapping("/comments/{commentId}")
    public ResponseEntity<CommentResponseDto> updateComment(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long commentId,
            @RequestBody UpdateCommentRequestDto request) {
        return ResponseEntity.ok(commentService.updateComment(userId, commentId, request));
    }

    @DeleteMapping("/comments/{commentId}")
    public ResponseEntity<Void> deleteComment(
            @AuthenticationPrincipal Long userId, @PathVariable Long commentId) {
        commentService.deleteComment(userId, commentId);
        return ResponseEntity.noContent().build();
    }
}
