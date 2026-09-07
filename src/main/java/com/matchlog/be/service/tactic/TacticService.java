package com.matchlog.be.service.tactic;

import com.matchlog.be.domain.match.Match;
import com.matchlog.be.domain.player.Player;
import com.matchlog.be.domain.tactic.PersonalTactic;
import com.matchlog.be.domain.tactic.TeamTactic;
import com.matchlog.be.dto.tactic.request.SaveTacticRequestDto;
import com.matchlog.be.dto.tactic.response.TacticResponseDto;
import com.matchlog.be.exception.CustomException;
import com.matchlog.be.exception.constant.MatchErrorCode;
import com.matchlog.be.exception.constant.PlayerErrorCode;
import com.matchlog.be.exception.constant.TacticErrorCode;
import com.matchlog.be.exception.constant.TeamErrorCode;
import com.matchlog.be.repository.MatchRepository;
import com.matchlog.be.repository.ParticipationRepository;
import com.matchlog.be.repository.PersonalTacticRepository;
import com.matchlog.be.repository.PlayerRepository;
import com.matchlog.be.repository.TeamTacticRepository;
import com.matchlog.be.service.player.PlayerService;
import com.matchlog.be.service.team.TeamAuthorizationService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TacticService {

    private final MatchRepository matchRepository;
    private final PlayerRepository playerRepository;
    private final TeamTacticRepository teamTacticRepository;
    private final PersonalTacticRepository personalTacticRepository;
    private final ParticipationRepository participationRepository;
    private final PlayerService playerService;
    private final TeamAuthorizationService teamAuthorizationService;

    @Transactional
    public TacticResponseDto saveTeamTactic(
            Long userId, Long matchId, SaveTacticRequestDto request) {
        Player player = playerService.getCurrentPlayer(userId);
        Match match = getMatch(matchId);

        teamAuthorizationService.requireManager(
                match.getTeam().getId(), player.getId(), "전술 작성 권한이 없습니다. (MANAGER만 가능)");

        if (match.isFinished()) {
            throw new CustomException(MatchErrorCode.MATCH_ALREADY_FINISHED);
        }

        TeamTactic tactic =
                teamTacticRepository
                        .findByMatch_Id(matchId)
                        .map(
                                existing -> {
                                    existing.update(
                                            request.getContent(),
                                            request.getDefensiveLineHeight(),
                                            request.getDefensiveSpacing(),
                                            request.getSetPieceDefense(),
                                            request.getFlankDefense(),
                                            request.getOffsideTrap(),
                                            request.getCrossType(),
                                            request.getSetPieceAttack(),
                                            request.getPossessionStrategy(),
                                            request.getPressingTrigger(),
                                            request.getPressingIntensity());
                                    return existing;
                                })
                        .orElseGet(
                                () ->
                                        teamTacticRepository.save(
                                                TeamTactic.create(
                                                        match,
                                                        player,
                                                        request.getContent(),
                                                        request.getDefensiveLineHeight(),
                                                        request.getDefensiveSpacing(),
                                                        request.getSetPieceDefense(),
                                                        request.getFlankDefense(),
                                                        request.getOffsideTrap(),
                                                        request.getCrossType(),
                                                        request.getSetPieceAttack(),
                                                        request.getPossessionStrategy(),
                                                        request.getPressingTrigger(),
                                                        request.getPressingIntensity())));

        return TacticResponseDto.from(tactic);
    }

    @Transactional(readOnly = true)
    public TacticResponseDto getTeamTactic(Long userId, Long matchId) {
        Player player = playerService.getCurrentPlayer(userId);
        Match match = getMatch(matchId);

        teamAuthorizationService.requireMember(match.getTeam().getId(), player.getId());

        TeamTactic tactic =
                teamTacticRepository
                        .findByMatch_Id(matchId)
                        .orElseThrow(() -> new CustomException(TacticErrorCode.TACTIC_NOT_FOUND));

        return TacticResponseDto.from(tactic);
    }

    @Transactional
    public TacticResponseDto savePersonalTactic(
            Long userId, Long matchId, Long playerId, SaveTacticRequestDto request) {
        Player coach = playerService.getCurrentPlayer(userId);
        Match match = getMatch(matchId);

        teamAuthorizationService.requireManager(
                match.getTeam().getId(), coach.getId(), "전술 작성 권한이 없습니다. (MANAGER만 가능)");

        if (match.isFinished()) {
            throw new CustomException(MatchErrorCode.MATCH_ALREADY_FINISHED);
        }

        Player targetPlayer =
                playerRepository
                        .findById(playerId)
                        .orElseThrow(() -> new CustomException(PlayerErrorCode.PLAYER_NOT_FOUND));

        if (!participationRepository.existsByTeam_IdAndPlayer_Id(
                match.getTeam().getId(), targetPlayer.getId())) {
            throw new CustomException(TeamErrorCode.MEMBER_NOT_FOUND);
        }

        PersonalTactic tactic =
                personalTacticRepository
                        .findByMatch_IdAndPlayer_Id(matchId, playerId)
                        .map(
                                existing -> {
                                    existing.updateContent(request.getContent());
                                    return existing;
                                })
                        .orElseGet(
                                () ->
                                        personalTacticRepository.save(
                                                PersonalTactic.create(
                                                        match,
                                                        targetPlayer,
                                                        coach,
                                                        request.getContent())));

        return TacticResponseDto.from(tactic);
    }

    @Transactional(readOnly = true)
    public List<TacticResponseDto> getAllPersonalTactics(Long userId, Long matchId) {
        Player player = playerService.getCurrentPlayer(userId);
        Match match = getMatch(matchId);

        teamAuthorizationService.requireMember(match.getTeam().getId(), player.getId());

        return personalTacticRepository.findByMatch_Id(matchId).stream()
                .map(TacticResponseDto::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public TacticResponseDto getPersonalTactic(Long userId, Long matchId, Long playerId) {
        Player player = playerService.getCurrentPlayer(userId);
        Match match = getMatch(matchId);

        teamAuthorizationService.requireMember(match.getTeam().getId(), player.getId());

        PersonalTactic tactic =
                personalTacticRepository
                        .findByMatch_IdAndPlayer_Id(matchId, playerId)
                        .orElseThrow(() -> new CustomException(TacticErrorCode.TACTIC_NOT_FOUND));

        return TacticResponseDto.from(tactic);
    }

    private Match getMatch(Long matchId) {
        return matchRepository
                .findById(matchId)
                .orElseThrow(() -> new CustomException(MatchErrorCode.MATCH_NOT_FOUND));
    }
}
