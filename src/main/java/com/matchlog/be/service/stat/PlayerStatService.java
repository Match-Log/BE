package com.matchlog.be.service.stat;

import com.matchlog.be.constant.stat.StatPeriod;
import com.matchlog.be.domain.participation.Participation;
import com.matchlog.be.domain.player.Player;
import com.matchlog.be.dto.stat.response.PlayerStatSummaryResponseDto;
import com.matchlog.be.dto.stat.response.TeamPlayerStatItemResponseDto;
import com.matchlog.be.dto.stat.response.TeamPlayerStatsResponseDto;
import com.matchlog.be.exception.CustomException;
import com.matchlog.be.exception.constant.CommonErrorCode;
import com.matchlog.be.exception.constant.TeamErrorCode;
import com.matchlog.be.repository.ParticipationRepository;
import com.matchlog.be.repository.PersonalFeedbackRepository;
import com.matchlog.be.repository.PlayerStatRepository;
import com.matchlog.be.repository.TeamRepository;
import com.matchlog.be.repository.projection.PlayerStatAggregate;
import com.matchlog.be.repository.projection.TeamPlayerStatRow;
import com.matchlog.be.service.player.PlayerService;
import com.matchlog.be.service.team.TeamAuthorizationService;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PlayerStatService {

    private final PlayerStatRepository playerStatRepository;
    private final PersonalFeedbackRepository personalFeedbackRepository;
    private final ParticipationRepository participationRepository;
    private final TeamRepository teamRepository;
    private final PlayerService playerService;
    private final TeamAuthorizationService teamAuthorizationService;

    @Transactional(readOnly = true)
    public PlayerStatSummaryResponseDto getStatSummary(
            Long teamId, Long playerId, Long userId, StatPeriod period, int year, Integer month) {
        Player requester = playerService.getCurrentPlayer(userId);
        teamAuthorizationService.requireMember(teamId, requester.getId());

        if (!participationRepository.existsByTeam_IdAndPlayer_Id(teamId, playerId)) {
            throw new CustomException(TeamErrorCode.MEMBER_NOT_FOUND);
        }

        if (period == StatPeriod.MONTHLY && (month == null || month < 1 || month > 12)) {
            throw new CustomException(
                    CommonErrorCode.INVALID_REQUEST_BODY,
                    "period=MONTHLY일 때 month는 1~12 사이여야 합니다.");
        }

        LocalDateTime from;
        LocalDateTime to;
        if (period == StatPeriod.MONTHLY) {
            from = LocalDateTime.of(year, month, 1, 0, 0);
            to = from.plusMonths(1);
        } else {
            from = LocalDateTime.of(year, 1, 1, 0, 0);
            to = from.plusYears(1);
        }

        PlayerStatAggregate aggregate =
                playerStatRepository.aggregateByPlayerAndPeriod(teamId, playerId, from, to);
        Double averageRating =
                personalFeedbackRepository.averageRatingByPlayerAndPeriod(
                        teamId, playerId, from, to);

        return PlayerStatSummaryResponseDto.builder()
                .playerId(playerId)
                .period(period)
                .year(year)
                .month(period == StatPeriod.MONTHLY ? month : null)
                .matchCount(aggregate.getMatchCount() == null ? 0 : aggregate.getMatchCount())
                .goals(aggregate.getGoals() == null ? 0 : aggregate.getGoals())
                .assists(aggregate.getAssists() == null ? 0 : aggregate.getAssists())
                .saves(aggregate.getSaves())
                .cleanSheetCount(aggregate.getCleanSheetCount())
                .mvpCount(aggregate.getMvpCount() == null ? 0 : aggregate.getMvpCount())
                .averageRating(averageRating)
                .build();
    }

    @Transactional(readOnly = true)
    public TeamPlayerStatsResponseDto getTeamPlayerStats(
            Long teamId, Long userId, StatPeriod period, Integer year, Integer month) {
        Player requester = playerService.getCurrentPlayer(userId);

        if (!teamRepository.existsById(teamId)) {
            throw new CustomException(TeamErrorCode.TEAM_NOT_FOUND);
        }

        teamAuthorizationService.requireMember(teamId, requester.getId());

        if (period != null && year == null) {
            throw new CustomException(
                    CommonErrorCode.INVALID_REQUEST_BODY, "period를 지정할 때는 year도 함께 입력해야 합니다.");
        }

        if (period == StatPeriod.MONTHLY && (month == null || month < 1 || month > 12)) {
            throw new CustomException(
                    CommonErrorCode.INVALID_REQUEST_BODY,
                    "period=MONTHLY일 때 month는 1~12 사이여야 합니다.");
        }

        LocalDateTime from;
        LocalDateTime to;
        if (year == null) {
            from = LocalDateTime.of(2000, 1, 1, 0, 0);
            to = LocalDateTime.of(2100, 1, 1, 0, 0);
        } else if (period == StatPeriod.MONTHLY) {
            from = LocalDateTime.of(year, month, 1, 0, 0);
            to = from.plusMonths(1);
        } else {
            from = LocalDateTime.of(year, 1, 1, 0, 0);
            to = from.plusYears(1);
        }

        List<Participation> roster = participationRepository.findRosterByTeamId(teamId);
        List<TeamPlayerStatRow> statRows =
                playerStatRepository.aggregateByTeamAndPeriod(teamId, from, to);
        Map<Long, TeamPlayerStatRow> statMap =
                statRows.stream().collect(Collectors.toMap(TeamPlayerStatRow::getPlayerId, r -> r));

        List<TeamPlayerStatItemResponseDto> players =
                roster.stream()
                        .map(
                                p -> {
                                    Long playerId = p.getPlayer().getId();
                                    TeamPlayerStatRow stat = statMap.get(playerId);
                                    return TeamPlayerStatItemResponseDto.builder()
                                            .playerId(playerId)
                                            .name(p.getPlayer().getUser().getName())
                                            .position(p.getMainPosition())
                                            .matchCount(
                                                    stat != null && stat.getMatchCount() != null
                                                            ? stat.getMatchCount()
                                                            : 0L)
                                            .goals(
                                                    stat != null && stat.getGoals() != null
                                                            ? stat.getGoals()
                                                            : 0L)
                                            .assists(
                                                    stat != null && stat.getAssists() != null
                                                            ? stat.getAssists()
                                                            : 0L)
                                            .saves(stat != null ? stat.getSaves() : null)
                                            .cleanSheetCount(
                                                    stat != null
                                                                    && stat.getCleanSheetCount()
                                                                            != null
                                                            ? stat.getCleanSheetCount()
                                                            : 0L)
                                            .mvpCount(
                                                    stat != null && stat.getMvpCount() != null
                                                            ? stat.getMvpCount()
                                                            : 0L)
                                            .build();
                                })
                        .toList();

        return TeamPlayerStatsResponseDto.builder().teamId(teamId).players(players).build();
    }
}
