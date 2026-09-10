package com.matchlog.be.service.match;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.matchlog.be.domain.match.Match;
import com.matchlog.be.domain.player.Player;
import com.matchlog.be.domain.team.Team;
import com.matchlog.be.exception.CustomException;
import com.matchlog.be.exception.constant.CommonErrorCode;
import com.matchlog.be.exception.constant.MatchErrorCode;
import com.matchlog.be.repository.DocumentRepository;
import com.matchlog.be.repository.MatchRepository;
import com.matchlog.be.repository.ParticipationRepository;
import com.matchlog.be.repository.TeamRepository;
import com.matchlog.be.service.player.PlayerService;
import com.matchlog.be.service.team.TeamAuthorizationService;
import java.util.Optional;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
class DeleteMatchUnitTest {

    private static final Long USER_ID = 1L;

    @Mock private MatchRepository matchRepository;
    @Mock private TeamRepository teamRepository;
    @Mock private ParticipationRepository participationRepository;
    @Mock private DocumentRepository documentRepository;
    @Mock private PlayerService playerService;
    @Mock private TeamAuthorizationService teamAuthorizationService;
    @InjectMocks private MatchService matchService;

    @Test
    void MANAGER면_경기_삭제에_성공한다() {
        Player player = Player.builder().id(9L).build();
        Team team = Team.builder().id(1L).build();
        Match match =
                Match.builder().id(100L).team(team).opponent("서울 드래곤즈").isFinished(false).build();

        when(playerService.getCurrentPlayer(USER_ID)).thenReturn(player);
        when(matchRepository.findById(100L)).thenReturn(Optional.of(match));
        doNothing().when(teamAuthorizationService).requireManager(eq(1L), eq(9L), anyString());

        matchService.deleteMatch(100L, USER_ID);

        verify(matchRepository, times(1)).delete(match);
    }

    @Test
    void 존재하지_않는_경기면_MATCH_NOT_FOUND_예외가_발생하고_삭제하지_않는다() {
        Player player = Player.builder().id(9L).build();

        when(playerService.getCurrentPlayer(USER_ID)).thenReturn(player);
        when(matchRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> matchService.deleteMatch(999L, USER_ID))
                .isInstanceOf(CustomException.class)
                .satisfies(
                        e ->
                                assertThat(((CustomException) e).getErrorCode())
                                        .isEqualTo(MatchErrorCode.MATCH_NOT_FOUND));

        verify(matchRepository, never()).delete(any(Match.class));
    }

    @Test
    void MANAGER가_아니면_FORBIDDEN_예외가_발생하고_삭제하지_않는다() {
        Player player = Player.builder().id(9L).build();
        Team team = Team.builder().id(1L).build();
        Match match =
                Match.builder().id(100L).team(team).opponent("서울 드래곤즈").isFinished(false).build();

        when(playerService.getCurrentPlayer(USER_ID)).thenReturn(player);
        when(matchRepository.findById(100L)).thenReturn(Optional.of(match));
        doThrow(new CustomException(CommonErrorCode.FORBIDDEN, "경기 삭제 권한이 없습니다. (MANAGER만 가능)"))
                .when(teamAuthorizationService)
                .requireManager(1L, 9L, "경기 삭제 권한이 없습니다. (MANAGER만 가능)");

        assertThatThrownBy(() -> matchService.deleteMatch(100L, USER_ID))
                .isInstanceOf(CustomException.class)
                .satisfies(
                        e ->
                                assertThat(((CustomException) e).getErrorCode())
                                        .isEqualTo(CommonErrorCode.FORBIDDEN));

        verify(matchRepository, never()).delete(match);
    }
}
