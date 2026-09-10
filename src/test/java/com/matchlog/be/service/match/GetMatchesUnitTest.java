package com.matchlog.be.service.match;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import com.matchlog.be.domain.match.Match;
import com.matchlog.be.domain.player.Player;
import com.matchlog.be.domain.team.Team;
import com.matchlog.be.dto.match.response.MatchListItemResponseDto;
import com.matchlog.be.exception.CustomException;
import com.matchlog.be.exception.constant.CommonErrorCode;
import com.matchlog.be.exception.constant.TeamErrorCode;
import com.matchlog.be.repository.DocumentRepository;
import com.matchlog.be.repository.MatchRepository;
import com.matchlog.be.repository.ParticipationRepository;
import com.matchlog.be.repository.TeamRepository;
import com.matchlog.be.service.player.PlayerService;
import com.matchlog.be.service.team.TeamAuthorizationService;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
class GetMatchesUnitTest {

    private static final Long USER_ID = 1L;

    @Mock private MatchRepository matchRepository;
    @Mock private TeamRepository teamRepository;
    @Mock private ParticipationRepository participationRepository;
    @Mock private DocumentRepository documentRepository;
    @Mock private PlayerService playerService;
    @Mock private TeamAuthorizationService teamAuthorizationService;
    @InjectMocks private MatchService matchService;

    @Test
    void status가_없으면_전체_경기_목록을_반환한다() {
        Player player = Player.builder().id(9L).build();
        Team team = Team.builder().id(1L).build();
        Match match =
                Match.builder()
                        .id(100L)
                        .team(team)
                        .opponent("서울 드래곤즈")
                        .matchDate(LocalDateTime.now())
                        .build();

        when(playerService.getCurrentPlayer(USER_ID)).thenReturn(player);
        when(teamRepository.findById(1L)).thenReturn(Optional.of(team));
        doNothing().when(teamAuthorizationService).requireMember(1L, 9L);
        when(matchRepository.findByTeam_IdOrderByMatchDateDesc(1L)).thenReturn(List.of(match));

        List<MatchListItemResponseDto> response = matchService.getMatches(1L, USER_ID, null);

        assertThat(response).hasSize(1);
        assertThat(response.get(0).getMatchId()).isEqualTo(100L);
    }

    @Test
    void status가_upcoming이면_예정된_경기만_조회한다() {
        Player player = Player.builder().id(9L).build();
        Team team = Team.builder().id(1L).build();

        when(playerService.getCurrentPlayer(USER_ID)).thenReturn(player);
        when(teamRepository.findById(1L)).thenReturn(Optional.of(team));
        doNothing().when(teamAuthorizationService).requireMember(1L, 9L);
        when(matchRepository.findByTeam_IdAndIsFinishedOrderByMatchDateDesc(1L, false))
                .thenReturn(List.of());

        List<MatchListItemResponseDto> response = matchService.getMatches(1L, USER_ID, "upcoming");

        assertThat(response).isEmpty();
    }

    @Test
    void status가_finished이면_종료된_경기만_조회한다() {
        Player player = Player.builder().id(9L).build();
        Team team = Team.builder().id(1L).build();

        when(playerService.getCurrentPlayer(USER_ID)).thenReturn(player);
        when(teamRepository.findById(1L)).thenReturn(Optional.of(team));
        doNothing().when(teamAuthorizationService).requireMember(1L, 9L);
        when(matchRepository.findByTeam_IdAndIsFinishedOrderByMatchDateDesc(1L, true))
                .thenReturn(List.of());

        List<MatchListItemResponseDto> response = matchService.getMatches(1L, USER_ID, "finished");

        assertThat(response).isEmpty();
    }

    @Test
    void 존재하지_않는_팀이면_TEAM_NOT_FOUND_예외가_발생한다() {
        Player player = Player.builder().id(9L).build();

        when(playerService.getCurrentPlayer(USER_ID)).thenReturn(player);
        when(teamRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> matchService.getMatches(999L, USER_ID, null))
                .isInstanceOf(CustomException.class)
                .satisfies(
                        e ->
                                assertThat(((CustomException) e).getErrorCode())
                                        .isEqualTo(TeamErrorCode.TEAM_NOT_FOUND));
    }

    @Test
    void 팀_소속이_아니면_FORBIDDEN_예외가_발생한다() {
        Player player = Player.builder().id(9L).build();
        Team team = Team.builder().id(1L).build();

        when(playerService.getCurrentPlayer(USER_ID)).thenReturn(player);
        when(teamRepository.findById(1L)).thenReturn(Optional.of(team));
        doThrow(new CustomException(CommonErrorCode.FORBIDDEN, "해당 팀에 접근 권한이 없습니다."))
                .when(teamAuthorizationService)
                .requireMember(1L, 9L);

        assertThatThrownBy(() -> matchService.getMatches(1L, USER_ID, null))
                .isInstanceOf(CustomException.class)
                .satisfies(
                        e ->
                                assertThat(((CustomException) e).getErrorCode())
                                        .isEqualTo(CommonErrorCode.FORBIDDEN));
    }
}
