package com.matchlog.be.service.comment;

import com.matchlog.be.domain.comment.Comment;
import com.matchlog.be.domain.document.Document;
import com.matchlog.be.domain.player.Player;
import com.matchlog.be.dto.comment.request.CreateCommentRequestDto;
import com.matchlog.be.dto.comment.request.UpdateCommentRequestDto;
import com.matchlog.be.dto.comment.response.CommentResponseDto;
import com.matchlog.be.exception.CustomException;
import com.matchlog.be.exception.constant.CommentErrorCode;
import com.matchlog.be.exception.constant.CommonErrorCode;
import com.matchlog.be.exception.constant.DocumentErrorCode;
import com.matchlog.be.repository.CommentRepository;
import com.matchlog.be.repository.DocumentRepository;
import com.matchlog.be.service.player.PlayerService;
import com.matchlog.be.service.team.TeamAuthorizationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final DocumentRepository documentRepository;
    private final PlayerService playerService;
    private final TeamAuthorizationService teamAuthorizationService;

    @Transactional
    public CommentResponseDto createComment(
            Long userId, Long boardId, CreateCommentRequestDto request) {
        Player player = playerService.getCurrentPlayer(userId);

        Document document =
                documentRepository
                        .findById(boardId)
                        .orElseThrow(() -> new CustomException(DocumentErrorCode.BOARD_NOT_FOUND));

        teamAuthorizationService.requireMember(document.getTeam().getId(), player.getId());

        Comment comment =
                commentRepository.save(Comment.create(document, player, request.getContent()));

        return CommentResponseDto.from(comment);
    }

    @Transactional
    public CommentResponseDto updateComment(
            Long userId, Long commentId, UpdateCommentRequestDto request) {
        Player player = playerService.getCurrentPlayer(userId);

        Comment comment =
                commentRepository
                        .findById(commentId)
                        .orElseThrow(() -> new CustomException(CommentErrorCode.COMMENT_NOT_FOUND));

        if (!comment.getPlayer().getId().equals(player.getId())) {
            throw new CustomException(CommonErrorCode.FORBIDDEN, "댓글 수정 권한이 없습니다.");
        }

        comment.updateContent(request.getContent());

        return CommentResponseDto.from(comment);
    }

    @Transactional
    public void deleteComment(Long userId, Long commentId) {
        Player player = playerService.getCurrentPlayer(userId);

        Comment comment =
                commentRepository
                        .findById(commentId)
                        .orElseThrow(() -> new CustomException(CommentErrorCode.COMMENT_NOT_FOUND));

        if (!comment.getPlayer().getId().equals(player.getId())) {
            throw new CustomException(CommonErrorCode.FORBIDDEN, "댓글 삭제 권한이 없습니다.");
        }

        commentRepository.delete(comment);
    }
}
