package com.matchlog.be.service.match;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import com.matchlog.be.constant.match.HomeAway;
import com.matchlog.be.constant.match.MatchType;
import com.matchlog.be.domain.match.Match;
import com.matchlog.be.domain.player.Player;
import com.matchlog.be.domain.team.Team;
import com.matchlog.be.dto.match.request.UpdateMatchRequestDto;
import com.matchlog.be.dto.match.response.UpdateMatchResponseDto;
import com.matchlog.be.exception.CustomException;
import com.matchlog.be.exception.constant.CommonErrorCode;
import com.matchlog.be.exception.constant.MatchErrorCode;
import com.matchlog.be.repository.DocumentRepository;
import com.matchlog.be.repository.MatchRepository;
import com.matchlog.be.repository.ParticipationRepository;
import com.matchlog.be.repository.TeamRepository;
import com.matchlog.be.service.player.PlayerService;
import com.matchlog.be.service.team.TeamAuthorizationService;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
class UpdateMatchUnitTest {

    private static final Long USER_ID = 1L;

    @Mock private MatchRepository matchRepository;
    @Mock private TeamRepository teamRepository;
    @Mock private ParticipationRepository participationRepository;
    @Mock private DocumentRepository documentRepository;
    @Mock private PlayerService playerService;
    @Mock private TeamAuthorizationService teamAuthorizationService;
    @InjectMocks private MatchService matchService;

    private Match finishableMatch(Team team) {
        return Match.builder()
                .id(100L)
                .team(team)
                .opponent("서울 드래곤즈")
                .matchDate(LocalDateTime.of(2099, 7, 14, 7, 0))
                .homeAway(HomeAway.HOME)
                .matchType(MatchType.SOCCER)
                .isFinished(false)
                .build();
    }

    @Test
    void MANAGER면_경기_종료_처리와_스코어_기록에_성공한다() {
        Player player = Player.builder().id(9L).build();
        Team team = Team.builder().id(1L).build();
        Match match = finishableMatch(team);
        UpdateMatchRequestDto request =
                UpdateMatchRequestDto.builder().scoreHome(3).scoreAway(1).isFinished(true).build();

        when(playerService.getCurrentPlayer(USER_ID)).thenReturn(player);
        when(matchRepository.findById(100L)).thenReturn(Optional.of(match));
        doNothing().when(teamAuthorizationService).requireManager(eq(1L), eq(9L), anyString());

        UpdateMatchResponseDto response = matchService.updateMatch(100L, USER_ID, request);

        assertThat(response.isFinished()).isTrue();
        assertThat(response.getScoreHome()).isEqualTo(3);
        assertThat(response.getScoreAway()).isEqualTo(1);
        assertThat(match.isFinished()).isTrue();
    }

    @Test
    void 존재하지_않는_경기면_MATCH_NOT_FOUND_예외가_발생한다() {
        Player player = Player.builder().id(9L).build();
        UpdateMatchRequestDto request = UpdateMatchRequestDto.builder().opponent("변경팀").build();

        when(playerService.getCurrentPlayer(USER_ID)).thenReturn(player);
        when(matchRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> matchService.updateMatch(999L, USER_ID, request))
                .isInstanceOf(CustomException.class)
                .satisfies(
                        e ->
                                assertThat(((CustomException) e).getErrorCode())
                                        .isEqualTo(MatchErrorCode.MATCH_NOT_FOUND));
    }

    @Test
    void MANAGER가_아니면_FORBIDDEN_예외가_발생한다() {
        Player player = Player.builder().id(9L).build();
        Team team = Team.builder().id(1L).build();
        Match match = finishableMatch(team);
        UpdateMatchRequestDto request = UpdateMatchRequestDto.builder().opponent("변경팀").build();

        when(playerService.getCurrentPlayer(USER_ID)).thenReturn(player);
        when(matchRepository.findById(100L)).thenReturn(Optional.of(match));
        doThrow(new CustomException(CommonErrorCode.FORBIDDEN, "경기 수정 권한이 없습니다. (MANAGER만 가능)"))
                .when(teamAuthorizationService)
                .requireManager(1L, 9L, "경기 수정 권한이 없습니다. (MANAGER만 가능)");

        assertThatThrownBy(() -> matchService.updateMatch(100L, USER_ID, request))
                .isInstanceOf(CustomException.class)
                .satisfies(
                        e ->
                                assertThat(((CustomException) e).getErrorCode())
                                        .isEqualTo(CommonErrorCode.FORBIDDEN));
    }

    @Test
    void 변경할_경기_날짜가_과거면_INVALID_MATCH_DATE_예외가_발생한다() {
        Player player = Player.builder().id(9L).build();
        Team team = Team.builder().id(1L).build();
        Match match = finishableMatch(team);
        UpdateMatchRequestDto request =
                UpdateMatchRequestDto.builder()
                        .matchDate(LocalDateTime.of(2020, 1, 1, 0, 0))
                        .build();

        when(playerService.getCurrentPlayer(USER_ID)).thenReturn(player);
        when(matchRepository.findById(100L)).thenReturn(Optional.of(match));
        doNothing().when(teamAuthorizationService).requireManager(eq(1L), eq(9L), anyString());

        assertThatThrownBy(() -> matchService.updateMatch(100L, USER_ID, request))
                .isInstanceOf(CustomException.class)
                .satisfies(
                        e ->
                                assertThat(((CustomException) e).getErrorCode())
                                        .isEqualTo(MatchErrorCode.INVALID_MATCH_DATE));
    }
}
