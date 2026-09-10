package com.matchlog.be.service.stat;

import com.matchlog.be.constant.lineup.Position;
import com.matchlog.be.constant.vote.VoteStatus;
import com.matchlog.be.domain.lineup.LineupSpot;
import com.matchlog.be.domain.match.Match;
import com.matchlog.be.domain.player.Player;
import com.matchlog.be.domain.stat.PlayerStat;
import com.matchlog.be.dto.stat.request.UpdatePlayerStatRequestDto;
import com.matchlog.be.dto.stat.response.MatchStatListResponseDto;
import com.matchlog.be.dto.stat.response.PlayerStatItemResponseDto;
import com.matchlog.be.dto.stat.response.PlayerStatResponseDto;
import com.matchlog.be.dto.stat.response.UpdatePlayerStatResponseDto;
import com.matchlog.be.exception.CustomException;
import com.matchlog.be.exception.constant.CommonErrorCode;
import com.matchlog.be.exception.constant.MatchErrorCode;
import com.matchlog.be.exception.constant.PlayerErrorCode;
import com.matchlog.be.repository.LineupSpotRepository;
import com.matchlog.be.repository.MatchRepository;
import com.matchlog.be.repository.ParticipationRepository;
import com.matchlog.be.repository.PlayerRepository;
import com.matchlog.be.repository.PlayerStatRepository;
import com.matchlog.be.repository.VoteRepository;
import com.matchlog.be.service.player.PlayerService;
import com.matchlog.be.service.team.TeamAuthorizationService;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class StatService {

    private final PlayerStatRepository playerStatRepository;
    private final MatchRepository matchRepository;
    private final VoteRepository voteRepository;
    private final PlayerRepository playerRepository;
    private final LineupSpotRepository lineupSpotRepository;
    private final ParticipationRepository participationRepository;
    private final PlayerService playerService;
    private final TeamAuthorizationService teamAuthorizationService;

    @Transactional(readOnly = true)
    public MatchStatListResponseDto getMatchStats(Long userId, Long matchId) {
        Player currentPlayer = playerService.getCurrentPlayer(userId);
        Match match = getMatch(matchId);
        teamAuthorizationService.requireMember(match.getTeam().getId(), currentPlayer.getId());

        if (!match.isFinished()) {
            throw new CustomException(MatchErrorCode.MATCH_NOT_FINISHED);
        }

        List<PlayerStat> stats = playerStatRepository.findStatsByMatchId(matchId);

        Map<Long, String> positionByPlayerId =
                buildPositionMap(matchId, match.getTeam().getId(), stats);

        List<PlayerStatItemResponseDto> items =
                stats.stream()
                        .map(
                                stat -> {
                                    String position =
                                            positionByPlayerId.getOrDefault(
                                                    stat.getPlayer().getId(), "UNKNOWN");
                                    return PlayerStatItemResponseDto.from(stat, position);
                                })
                        .toList();

        return MatchStatListResponseDto.builder().matchId(matchId).stats(items).build();
    }

    @Transactional(readOnly = true)
    public PlayerStatResponseDto getPlayerStat(Long userId, Long matchId, Long playerId) {
        Player currentPlayer = playerService.getCurrentPlayer(userId);
        Match match = getMatch(matchId);
        teamAuthorizationService.requireMember(match.getTeam().getId(), currentPlayer.getId());

        if (!match.isFinished()) {
            throw new CustomException(MatchErrorCode.MATCH_NOT_FINISHED);
        }

        if (!voteRepository.existsByMatch_IdAndPlayer_IdAndStatus(
                matchId, playerId, VoteStatus.ATTEND)) {
            throw new CustomException(MatchErrorCode.PLAYER_NOT_ATTENDED);
        }

        return playerStatRepository
                .findByMatch_IdAndPlayer_Id(matchId, playerId)
                .map(PlayerStatResponseDto::from)
                .orElseGet(
                        () -> {
                            Player target =
                                    playerRepository
                                            .findById(playerId)
                                            .orElseThrow(
                                                    () ->
                                                            new CustomException(
                                                                    PlayerErrorCode
                                                                            .PLAYER_NOT_FOUND));
                            return PlayerStatResponseDto.defaultOf(
                                    matchId, playerId, target.getUser().getName());
                        });
    }

    @Transactional
    public UpdatePlayerStatResponseDto updatePlayerStat(
            Long userId, Long matchId, Long playerId, UpdatePlayerStatRequestDto request) {
        Player currentPlayer = playerService.getCurrentPlayer(userId);
        Match match = getMatch(matchId);
        teamAuthorizationService.requireMember(match.getTeam().getId(), currentPlayer.getId());

        if (!currentPlayer.getId().equals(playerId)) {
            throw new CustomException(CommonErrorCode.FORBIDDEN, "본인 스탯만 입력할 수 있습니다.");
        }

        if (!match.isFinished()) {
            throw new CustomException(MatchErrorCode.MATCH_NOT_FINISHED);
        }

        Player targetPlayer =
                playerRepository
                        .findById(playerId)
                        .orElseThrow(() -> new CustomException(PlayerErrorCode.PLAYER_NOT_FOUND));

        if (!voteRepository.existsByMatch_IdAndPlayer_IdAndStatus(
                matchId, playerId, VoteStatus.ATTEND)) {
            throw new CustomException(MatchErrorCode.PLAYER_NOT_ATTENDED);
        }

        PlayerStat stat =
                playerStatRepository
                        .findByMatch_IdAndPlayer_Id(matchId, playerId)
                        .map(
                                existing -> {
                                    existing.updateStats(
                                            request.getGoals(),
                                            request.getAssists(),
                                            request.getSaves());
                                    return existing;
                                })
                        .orElseGet(
                                () -> {
                                    PlayerStat created =
                                            playerStatRepository.save(
                                                    PlayerStat.create(match, targetPlayer));
                                    created.updateStats(
                                            request.getGoals(),
                                            request.getAssists(),
                                            request.getSaves());
                                    return created;
                                });

        return UpdatePlayerStatResponseDto.from(stat);
    }

    private Match getMatch(Long matchId) {
        return matchRepository
                .findById(matchId)
                .orElseThrow(() -> new CustomException(MatchErrorCode.MATCH_NOT_FOUND));
    }

    private Map<Long, String> buildPositionMap(Long matchId, Long teamId, List<PlayerStat> stats) {
        Map<Long, String> positionMap =
                lineupSpotRepository.findSpotsByMatchId(matchId).stream()
                        .filter(spot -> spot.getPlayer() != null)
                        .collect(
                                Collectors.toMap(
                                        spot -> spot.getPlayer().getId(),
                                        LineupSpot::getPosition,
                                        (a, b) -> a));

        for (PlayerStat stat : stats) {
            Long playerId = stat.getPlayer().getId();
            if (!positionMap.containsKey(playerId)) {
                participationRepository
                        .findByTeam_IdAndPlayer_Id(teamId, playerId)
                        .map(p -> p.getMainPosition())
                        .filter(pos -> pos != null)
                        .map(Position::name)
                        .ifPresent(pos -> positionMap.put(playerId, pos));
            }
        }

        return positionMap;
    }
}
