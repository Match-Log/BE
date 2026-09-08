package com.matchlog.be.service.feedback;

import com.matchlog.be.constant.participation.ParticipationRole;
import com.matchlog.be.constant.vote.VoteStatus;
import com.matchlog.be.domain.feedback.PersonalFeedback;
import com.matchlog.be.domain.feedback.TeamFeedback;
import com.matchlog.be.domain.match.Match;
import com.matchlog.be.domain.player.Player;
import com.matchlog.be.dto.feedback.request.SavePersonalFeedbackRequestDto;
import com.matchlog.be.dto.feedback.request.SaveTeamFeedbackRequestDto;
import com.matchlog.be.dto.feedback.response.FeedbackResponseDto;
import com.matchlog.be.exception.CustomException;
import com.matchlog.be.exception.constant.CommonErrorCode;
import com.matchlog.be.exception.constant.FeedbackErrorCode;
import com.matchlog.be.exception.constant.MatchErrorCode;
import com.matchlog.be.exception.constant.PlayerErrorCode;
import com.matchlog.be.exception.constant.TeamErrorCode;
import com.matchlog.be.repository.MatchRepository;
import com.matchlog.be.repository.ParticipationRepository;
import com.matchlog.be.repository.PersonalFeedbackRepository;
import com.matchlog.be.repository.PlayerRepository;
import com.matchlog.be.repository.TeamFeedbackRepository;
import com.matchlog.be.repository.VoteRepository;
import com.matchlog.be.service.player.PlayerService;
import com.matchlog.be.service.team.TeamAuthorizationService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FeedbackService {

    private final MatchRepository matchRepository;
    private final PlayerRepository playerRepository;
    private final TeamFeedbackRepository teamFeedbackRepository;
    private final PersonalFeedbackRepository personalFeedbackRepository;
    private final ParticipationRepository participationRepository;
    private final VoteRepository voteRepository;
    private final PlayerService playerService;
    private final TeamAuthorizationService teamAuthorizationService;

    @Transactional
    public FeedbackResponseDto saveTeamFeedback(
            Long userId, Long matchId, SaveTeamFeedbackRequestDto request) {
        Player player = playerService.getCurrentPlayer(userId);
        Match match = getMatch(matchId);

        teamAuthorizationService.requireManager(
                match.getTeam().getId(), player.getId(), "피드백 작성 권한이 없습니다. (MANAGER만 가능)");

        if (!match.isFinished()) {
            throw new CustomException(
                    MatchErrorCode.MATCH_NOT_FINISHED, "종료된 경기에만 피드백을 작성할 수 있습니다.");
        }

        TeamFeedback feedback =
                teamFeedbackRepository
                        .findByMatch_Id(matchId)
                        .map(
                                existing -> {
                                    existing.updateContent(request.getContent());
                                    return existing;
                                })
                        .orElseGet(
                                () ->
                                        teamFeedbackRepository.save(
                                                TeamFeedback.create(
                                                        match, player, request.getContent())));

        return FeedbackResponseDto.from(feedback);
    }

    @Transactional(readOnly = true)
    public FeedbackResponseDto getTeamFeedback(Long userId, Long matchId) {
        Player player = playerService.getCurrentPlayer(userId);
        Match match = getMatch(matchId);

        teamAuthorizationService.requireMember(match.getTeam().getId(), player.getId());

        if (!match.isFinished()) {
            throw new CustomException(
                    MatchErrorCode.MATCH_NOT_FINISHED, "종료된 경기에만 피드백을 조회할 수 있습니다.");
        }

        TeamFeedback feedback =
                teamFeedbackRepository
                        .findByMatch_Id(matchId)
                        .orElseThrow(
                                () -> new CustomException(FeedbackErrorCode.FEEDBACK_NOT_FOUND));

        return FeedbackResponseDto.from(feedback);
    }

    @Transactional
    public FeedbackResponseDto savePersonalFeedback(
            Long userId, Long matchId, Long playerId, SavePersonalFeedbackRequestDto request) {
        Player coach = playerService.getCurrentPlayer(userId);
        Match match = getMatch(matchId);

        teamAuthorizationService.requireManager(
                match.getTeam().getId(), coach.getId(), "피드백 작성 권한이 없습니다. (MANAGER만 가능)");

        if (!match.isFinished()) {
            throw new CustomException(
                    MatchErrorCode.MATCH_NOT_FINISHED, "종료된 경기에만 피드백을 작성할 수 있습니다.");
        }

        Player targetPlayer =
                playerRepository
                        .findById(playerId)
                        .orElseThrow(() -> new CustomException(PlayerErrorCode.PLAYER_NOT_FOUND));

        if (!participationRepository.existsByTeam_IdAndPlayer_Id(
                match.getTeam().getId(), targetPlayer.getId())) {
            throw new CustomException(TeamErrorCode.MEMBER_NOT_FOUND);
        }

        if (!voteRepository.existsByMatch_IdAndPlayer_IdAndStatus(
                matchId, targetPlayer.getId(), VoteStatus.ATTEND)) {
            throw new CustomException(MatchErrorCode.PLAYER_NOT_ATTENDED);
        }

        PersonalFeedback feedback =
                personalFeedbackRepository
                        .findByMatch_IdAndPlayer_Id(matchId, playerId)
                        .map(
                                existing -> {
                                    existing.update(
                                            request.getContent(),
                                            request.getRating(),
                                            request.getPros(),
                                            request.getCons(),
                                            request.getTags(),
                                            request.getIsVisible());
                                    return existing;
                                })
                        .orElseGet(
                                () ->
                                        personalFeedbackRepository.save(
                                                PersonalFeedback.create(
                                                        match,
                                                        targetPlayer,
                                                        coach,
                                                        request.getContent(),
                                                        request.getRating(),
                                                        request.getPros(),
                                                        request.getCons(),
                                                        request.getTags(),
                                                        request.getIsVisible() != null
                                                                ? request.getIsVisible()
                                                                : true)));

        return FeedbackResponseDto.from(feedback);
    }

    @Transactional(readOnly = true)
    public List<FeedbackResponseDto> getAllPersonalFeedbacks(Long userId, Long matchId) {
        Player coach = playerService.getCurrentPlayer(userId);
        Match match = getMatch(matchId);

        teamAuthorizationService.requireManager(
                match.getTeam().getId(), coach.getId(), "피드백 조회 권한이 없습니다. (MANAGER만 가능)");

        return personalFeedbackRepository.findByMatch_Id(matchId).stream()
                .map(FeedbackResponseDto::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public FeedbackResponseDto getPersonalFeedback(Long userId, Long matchId, Long playerId) {
        Player player = playerService.getCurrentPlayer(userId);
        Match match = getMatch(matchId);

        ParticipationRole role =
                teamAuthorizationService.getRole(match.getTeam().getId(), player.getId());

        boolean isManager = role == ParticipationRole.MANAGER;
        boolean isSelf = player.getId().equals(playerId);

        if (!isManager && !isSelf) {
            throw new CustomException(CommonErrorCode.FORBIDDEN, "해당 피드백에 접근 권한이 없습니다.");
        }

        PersonalFeedback feedback =
                personalFeedbackRepository
                        .findByMatch_IdAndPlayer_Id(matchId, playerId)
                        .orElseThrow(
                                () -> new CustomException(FeedbackErrorCode.FEEDBACK_NOT_FOUND));

        if (!isManager && !feedback.isVisible()) {
            throw new CustomException(FeedbackErrorCode.FEEDBACK_NOT_FOUND);
        }

        return FeedbackResponseDto.from(feedback);
    }

    private Match getMatch(Long matchId) {
        return matchRepository
                .findById(matchId)
                .orElseThrow(() -> new CustomException(MatchErrorCode.MATCH_NOT_FOUND));
    }
}
