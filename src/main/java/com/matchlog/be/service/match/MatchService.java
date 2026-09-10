package com.matchlog.be.service.match;

import com.matchlog.be.domain.document.Document;
import com.matchlog.be.domain.match.Match;
import com.matchlog.be.domain.player.Player;
import com.matchlog.be.domain.team.Team;
import com.matchlog.be.dto.match.request.CreateMatchRequestDto;
import com.matchlog.be.dto.match.request.UpdateMatchRequestDto;
import com.matchlog.be.dto.match.response.CreateMatchResponseDto;
import com.matchlog.be.dto.match.response.MatchListItemResponseDto;
import com.matchlog.be.dto.match.response.MatchResponseDto;
import com.matchlog.be.dto.match.response.UpdateMatchResponseDto;
import com.matchlog.be.exception.CustomException;
import com.matchlog.be.exception.constant.CommonErrorCode;
import com.matchlog.be.exception.constant.MatchErrorCode;
import com.matchlog.be.exception.constant.TeamErrorCode;
import com.matchlog.be.repository.DocumentRepository;
import com.matchlog.be.repository.MatchRepository;
import com.matchlog.be.repository.ParticipationRepository;
import com.matchlog.be.repository.TeamRepository;
import com.matchlog.be.service.player.PlayerService;
import com.matchlog.be.service.team.TeamAuthorizationService;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MatchService {

    private final MatchRepository matchRepository;
    private final TeamRepository teamRepository;
    private final ParticipationRepository participationRepository;
    private final DocumentRepository documentRepository;
    private final PlayerService playerService;
    private final TeamAuthorizationService teamAuthorizationService;

    @Transactional
    public CreateMatchResponseDto createMatch(
            Long teamId, Long userId, CreateMatchRequestDto request) {
        Player player = playerService.getCurrentPlayer(userId);

        Team team =
                teamRepository
                        .findById(teamId)
                        .orElseThrow(() -> new CustomException(TeamErrorCode.TEAM_NOT_FOUND));

        teamAuthorizationService.requireManager(
                teamId, player.getId(), "경기 생성 권한이 없습니다. (MANAGER만 가능)");

        if (request.getMatchDate().isBefore(LocalDateTime.now())) {
            throw new CustomException(MatchErrorCode.INVALID_MATCH_DATE);
        }

        if (matchRepository.existsByTeam_IdAndMatchDate(teamId, request.getMatchDate())) {
            throw new CustomException(MatchErrorCode.MATCH_ALREADY_EXISTS);
        }

        Match match =
                matchRepository.save(
                        Match.create(
                                team,
                                request.getOpponent(),
                                request.getMatchDate(),
                                request.getLocation(),
                                request.getHomeAway(),
                                request.getMatchType()));

        Document board =
                documentRepository.save(
                        Document.create(
                                team,
                                player,
                                match,
                                buildBoardTitle(match),
                                "경기 참석 여부를 투표해주세요.",
                                true));

        return CreateMatchResponseDto.from(match, board.getId(), board.getTitle());
    }

    public List<MatchListItemResponseDto> getMatches(Long teamId, Long userId, String status) {
        Player player = playerService.getCurrentPlayer(userId);

        teamRepository
                .findById(teamId)
                .orElseThrow(() -> new CustomException(TeamErrorCode.TEAM_NOT_FOUND));

        teamAuthorizationService.requireMember(teamId, player.getId());

        List<Match> matches;
        if ("upcoming".equals(status)) {
            matches = matchRepository.findByTeam_IdAndIsFinishedOrderByMatchDateDesc(teamId, false);
        } else if ("finished".equals(status)) {
            matches = matchRepository.findByTeam_IdAndIsFinishedOrderByMatchDateDesc(teamId, true);
        } else {
            matches = matchRepository.findByTeam_IdOrderByMatchDateDesc(teamId);
        }

        return matches.stream().map(MatchListItemResponseDto::from).toList();
    }

    public MatchResponseDto getMatch(Long matchId, Long userId) {
        Player player = playerService.getCurrentPlayer(userId);

        Match match =
                matchRepository
                        .findById(matchId)
                        .orElseThrow(() -> new CustomException(MatchErrorCode.MATCH_NOT_FOUND));

        if (!participationRepository.existsByTeam_IdAndPlayer_Id(
                match.getTeam().getId(), player.getId())) {
            throw new CustomException(CommonErrorCode.FORBIDDEN, "해당 경기에 접근 권한이 없습니다.");
        }

        return MatchResponseDto.from(match);
    }

    @Transactional
    public UpdateMatchResponseDto updateMatch(
            Long matchId, Long userId, UpdateMatchRequestDto request) {
        Player player = playerService.getCurrentPlayer(userId);

        Match match =
                matchRepository
                        .findById(matchId)
                        .orElseThrow(() -> new CustomException(MatchErrorCode.MATCH_NOT_FOUND));

        teamAuthorizationService.requireManager(
                match.getTeam().getId(), player.getId(), "경기 수정 권한이 없습니다. (MANAGER만 가능)");

        if (request.getMatchDate() != null
                && request.getMatchDate().isBefore(LocalDateTime.now())) {
            throw new CustomException(MatchErrorCode.INVALID_MATCH_DATE);
        }

        match.updateSchedule(
                request.getOpponent(),
                request.getMatchDate(),
                request.getLocation(),
                request.getHomeAway(),
                request.getMatchType());

        if (request.getScoreHome() != null || request.getScoreAway() != null) {
            match.recordScore(request.getScoreHome(), request.getScoreAway());
        }

        if (Boolean.TRUE.equals(request.getIsFinished())) {
            match.changeStatusAsFinished();
        }

        return UpdateMatchResponseDto.from(match);
    }

    @Transactional
    public void deleteMatch(Long matchId, Long userId) {
        Player player = playerService.getCurrentPlayer(userId);

        Match match =
                matchRepository
                        .findById(matchId)
                        .orElseThrow(() -> new CustomException(MatchErrorCode.MATCH_NOT_FOUND));

        teamAuthorizationService.requireManager(
                match.getTeam().getId(), player.getId(), "경기 삭제 권한이 없습니다. (MANAGER만 가능)");

        matchRepository.delete(match);
    }

    private String buildBoardTitle(Match match) {
        LocalDateTime date = match.getMatchDate();
        return "%d/%d vs %s 참석 투표"
                .formatted(date.getMonthValue(), date.getDayOfMonth(), match.getOpponent());
    }
}
