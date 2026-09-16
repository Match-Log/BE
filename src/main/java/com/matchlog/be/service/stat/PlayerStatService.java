package com.matchlog.be.service.stat;

import com.matchlog.be.constant.stat.StatPeriod;
import com.matchlog.be.domain.player.Player;
import com.matchlog.be.dto.stat.response.PlayerStatSummaryResponseDto;
import com.matchlog.be.exception.CustomException;
import com.matchlog.be.exception.constant.CommonErrorCode;
import com.matchlog.be.exception.constant.TeamErrorCode;
import com.matchlog.be.repository.ParticipationRepository;
import com.matchlog.be.repository.PersonalFeedbackRepository;
import com.matchlog.be.repository.PlayerStatRepository;
import com.matchlog.be.repository.projection.PlayerStatAggregate;
import com.matchlog.be.service.player.PlayerService;
import com.matchlog.be.service.team.TeamAuthorizationService;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PlayerStatService {

    private final PlayerStatRepository playerStatRepository;
    private final PersonalFeedbackRepository personalFeedbackRepository;
    private final ParticipationRepository participationRepository;
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
}
