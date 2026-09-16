package com.matchlog.be.service.stat;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
class GetPlayerStatSummaryUnitTest {

    private static final Long USER_ID = 1L;
    private static final Long TEAM_ID = 1L;
    private static final Long PLAYER_ID = 3L;

    @Mock private PlayerStatRepository playerStatRepository;
    @Mock private PersonalFeedbackRepository personalFeedbackRepository;
    @Mock private ParticipationRepository participationRepository;
    @Mock private PlayerService playerService;
    @Mock private TeamAuthorizationService teamAuthorizationService;
    @InjectMocks private PlayerStatService playerStatService;

    @Test
    void 월별_집계를_정상적으로_반환한다() {
        Player requester = Player.builder().id(2L).build();
        when(playerService.getCurrentPlayer(USER_ID)).thenReturn(requester);
        doNothing().when(teamAuthorizationService).requireMember(TEAM_ID, 2L);
        when(participationRepository.existsByTeam_IdAndPlayer_Id(TEAM_ID, PLAYER_ID))
                .thenReturn(true);

        LocalDateTime from = LocalDateTime.of(2026, 9, 1, 0, 0);
        LocalDateTime to = LocalDateTime.of(2026, 10, 1, 0, 0);

        PlayerStatAggregate aggregate = mock(PlayerStatAggregate.class);
        when(aggregate.getMatchCount()).thenReturn(4L);
        when(aggregate.getGoals()).thenReturn(2L);
        when(aggregate.getAssists()).thenReturn(1L);
        when(aggregate.getSaves()).thenReturn(null);
        when(aggregate.getCleanSheetCount()).thenReturn(null);
        when(aggregate.getMvpCount()).thenReturn(1L);
        when(playerStatRepository.aggregateByPlayerAndPeriod(TEAM_ID, PLAYER_ID, from, to))
                .thenReturn(aggregate);
        when(personalFeedbackRepository.averageRatingByPlayerAndPeriod(TEAM_ID, PLAYER_ID, from, to))
                .thenReturn(7.5);

        PlayerStatSummaryResponseDto response =
                playerStatService.getStatSummary(
                        TEAM_ID, PLAYER_ID, USER_ID, StatPeriod.MONTHLY, 2026, 9);

        assertThat(response.getPlayerId()).isEqualTo(PLAYER_ID);
        assertThat(response.getPeriod()).isEqualTo(StatPeriod.MONTHLY);
        assertThat(response.getYear()).isEqualTo(2026);
        assertThat(response.getMonth()).isEqualTo(9);
        assertThat(response.getMatchCount()).isEqualTo(4);
        assertThat(response.getGoals()).isEqualTo(2);
        assertThat(response.getAssists()).isEqualTo(1);
        assertThat(response.getSaves()).isNull();
        assertThat(response.getMvpCount()).isEqualTo(1);
        assertThat(response.getAverageRating()).isEqualTo(7.5);
    }

    @Test
    void 시즌_집계는_month가_null로_내려간다() {
        Player requester = Player.builder().id(2L).build();
        when(playerService.getCurrentPlayer(USER_ID)).thenReturn(requester);
        doNothing().when(teamAuthorizationService).requireMember(TEAM_ID, 2L);
        when(participationRepository.existsByTeam_IdAndPlayer_Id(TEAM_ID, PLAYER_ID))
                .thenReturn(true);

        LocalDateTime from = LocalDateTime.of(2026, 1, 1, 0, 0);
        LocalDateTime to = LocalDateTime.of(2027, 1, 1, 0, 0);

        PlayerStatAggregate aggregate = mock(PlayerStatAggregate.class);
        when(aggregate.getMatchCount()).thenReturn(0L);
        when(aggregate.getGoals()).thenReturn(0L);
        when(aggregate.getAssists()).thenReturn(0L);
        when(aggregate.getSaves()).thenReturn(null);
        when(aggregate.getCleanSheetCount()).thenReturn(null);
        when(aggregate.getMvpCount()).thenReturn(0L);
        when(playerStatRepository.aggregateByPlayerAndPeriod(TEAM_ID, PLAYER_ID, from, to))
                .thenReturn(aggregate);
        when(personalFeedbackRepository.averageRatingByPlayerAndPeriod(TEAM_ID, PLAYER_ID, from, to))
                .thenReturn(null);

        PlayerStatSummaryResponseDto response =
                playerStatService.getStatSummary(
                        TEAM_ID, PLAYER_ID, USER_ID, StatPeriod.SEASON, 2026, null);

        assertThat(response.getPeriod()).isEqualTo(StatPeriod.SEASON);
        assertThat(response.getMonth()).isNull();
        assertThat(response.getMatchCount()).isEqualTo(0);
        assertThat(response.getAverageRating()).isNull();
    }

    @Test
    void MONTHLY인데_month가_없으면_INVALID_REQUEST_BODY_예외가_발생한다() {
        Player requester = Player.builder().id(2L).build();
        when(playerService.getCurrentPlayer(USER_ID)).thenReturn(requester);
        doNothing().when(teamAuthorizationService).requireMember(TEAM_ID, 2L);
        when(participationRepository.existsByTeam_IdAndPlayer_Id(TEAM_ID, PLAYER_ID))
                .thenReturn(true);

        assertThatThrownBy(
                        () ->
                                playerStatService.getStatSummary(
                                        TEAM_ID, PLAYER_ID, USER_ID, StatPeriod.MONTHLY, 2026, null))
                .isInstanceOf(CustomException.class)
                .satisfies(
                        e ->
                                assertThat(((CustomException) e).getErrorCode())
                                        .isEqualTo(CommonErrorCode.INVALID_REQUEST_BODY));
    }

    @Test
    void 대상_선수가_팀_소속이_아니면_MEMBER_NOT_FOUND_예외가_발생한다() {
        Player requester = Player.builder().id(2L).build();
        when(playerService.getCurrentPlayer(USER_ID)).thenReturn(requester);
        doNothing().when(teamAuthorizationService).requireMember(TEAM_ID, 2L);
        when(participationRepository.existsByTeam_IdAndPlayer_Id(eq(TEAM_ID), eq(PLAYER_ID)))
                .thenReturn(false);

        assertThatThrownBy(
                        () ->
                                playerStatService.getStatSummary(
                                        TEAM_ID, PLAYER_ID, USER_ID, StatPeriod.SEASON, 2026, null))
                .isInstanceOf(CustomException.class)
                .satisfies(
                        e ->
                                assertThat(((CustomException) e).getErrorCode())
                                        .isEqualTo(TeamErrorCode.MEMBER_NOT_FOUND));
    }

    @Test
    void 요청자가_팀_소속이_아니면_FORBIDDEN_예외가_발생한다() {
        Player requester = Player.builder().id(2L).build();
        when(playerService.getCurrentPlayer(USER_ID)).thenReturn(requester);
        doThrow(new CustomException(CommonErrorCode.FORBIDDEN, "해당 팀에 접근 권한이 없습니다."))
                .when(teamAuthorizationService)
                .requireMember(TEAM_ID, 2L);

        assertThatThrownBy(
                        () ->
                                playerStatService.getStatSummary(
                                        TEAM_ID, PLAYER_ID, USER_ID, StatPeriod.SEASON, 2026, null))
                .isInstanceOf(CustomException.class)
                .satisfies(
                        e ->
                                assertThat(((CustomException) e).getErrorCode())
                                        .isEqualTo(CommonErrorCode.FORBIDDEN));
    }
}