package com.matchlog.be.service.document;

import com.matchlog.be.constant.participation.ParticipationRole;
import com.matchlog.be.domain.document.Document;
import com.matchlog.be.domain.match.Match;
import com.matchlog.be.domain.player.Player;
import com.matchlog.be.domain.team.Team;
import com.matchlog.be.dto.document.request.CreateDocumentRequestDto;
import com.matchlog.be.dto.document.request.UpdateDocumentRequestDto;
import com.matchlog.be.dto.document.response.CreateDocumentResponseDto;
import com.matchlog.be.dto.document.response.DocumentListItemResponseDto;
import com.matchlog.be.dto.document.response.DocumentResponseDto;
import com.matchlog.be.dto.document.response.PinDocumentResponseDto;
import com.matchlog.be.dto.document.response.UpdateDocumentResponseDto;
import com.matchlog.be.exception.CustomException;
import com.matchlog.be.exception.constant.CommonErrorCode;
import com.matchlog.be.exception.constant.DocumentErrorCode;
import com.matchlog.be.exception.constant.MatchErrorCode;
import com.matchlog.be.exception.constant.TeamErrorCode;
import com.matchlog.be.repository.DocumentRepository;
import com.matchlog.be.repository.MatchRepository;
import com.matchlog.be.repository.TeamRepository;
import com.matchlog.be.service.player.PlayerService;
import com.matchlog.be.service.team.TeamAuthorizationService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DocumentService {

    private final DocumentRepository documentRepository;
    private final TeamRepository teamRepository;
    private final MatchRepository matchRepository;
    private final PlayerService playerService;
    private final TeamAuthorizationService teamAuthorizationService;

    @Transactional
    public CreateDocumentResponseDto createDocument(Long userId, CreateDocumentRequestDto request) {
        Player player = playerService.getCurrentPlayer(userId);

        Team team =
                teamRepository
                        .findById(request.getTeamId())
                        .orElseThrow(() -> new CustomException(TeamErrorCode.TEAM_NOT_FOUND));

        ParticipationRole role = teamAuthorizationService.getRole(team.getId(), player.getId());

        Match match = null;
        if (request.getMatchId() != null) {
            match =
                    matchRepository
                            .findById(request.getMatchId())
                            .orElseThrow(() -> new CustomException(MatchErrorCode.MATCH_NOT_FOUND));
        }

        boolean isPinned = role == ParticipationRole.MANAGER && request.isPinned();

        Document document =
                documentRepository.save(
                        Document.create(
                                team,
                                player,
                                match,
                                request.getTitle(),
                                request.getContent(),
                                isPinned));

        return CreateDocumentResponseDto.from(document);
    }

    public List<DocumentListItemResponseDto> getDocuments(Long userId, Long teamId) {
        Player player = playerService.getCurrentPlayer(userId);

        teamAuthorizationService.requireMember(teamId, player.getId());

        return documentRepository.findByTeam_IdOrderByPinnedAndCreatedAt(teamId).stream()
                .map(DocumentListItemResponseDto::from)
                .toList();
    }

    public DocumentResponseDto getDocument(Long userId, Long boardId) {
        Player player = playerService.getCurrentPlayer(userId);

        Document document =
                documentRepository
                        .findByIdWithPlayer(boardId)
                        .orElseThrow(() -> new CustomException(DocumentErrorCode.BOARD_NOT_FOUND));

        teamAuthorizationService.requireMember(document.getTeam().getId(), player.getId());

        return DocumentResponseDto.from(document);
    }

    @Transactional
    public UpdateDocumentResponseDto updateDocument(
            Long userId, Long boardId, UpdateDocumentRequestDto request) {
        Player player = playerService.getCurrentPlayer(userId);

        Document document =
                documentRepository
                        .findById(boardId)
                        .orElseThrow(() -> new CustomException(DocumentErrorCode.BOARD_NOT_FOUND));

        if (!document.getPlayer().getId().equals(player.getId())) {
            throw new CustomException(CommonErrorCode.FORBIDDEN, "게시글 수정 권한이 없습니다.");
        }

        document.updateContent(request.getTitle(), request.getContent());

        return UpdateDocumentResponseDto.from(document);
    }

    @Transactional
    public void deleteDocument(Long userId, Long boardId) {
        Player player = playerService.getCurrentPlayer(userId);

        Document document =
                documentRepository
                        .findById(boardId)
                        .orElseThrow(() -> new CustomException(DocumentErrorCode.BOARD_NOT_FOUND));

        if (!document.getPlayer().getId().equals(player.getId())) {
            throw new CustomException(CommonErrorCode.FORBIDDEN, "게시글 삭제 권한이 없습니다.");
        }

        documentRepository.delete(document);
    }

    @Transactional
    public PinDocumentResponseDto togglePin(Long userId, Long boardId) {
        Player player = playerService.getCurrentPlayer(userId);

        Document document =
                documentRepository
                        .findById(boardId)
                        .orElseThrow(() -> new CustomException(DocumentErrorCode.BOARD_NOT_FOUND));

        teamAuthorizationService.requireManager(
                document.getTeam().getId(), player.getId(), "핀 설정 권한이 없습니다. (MANAGER만 가능)");

        document.togglePin();

        return PinDocumentResponseDto.from(document);
    }
}
