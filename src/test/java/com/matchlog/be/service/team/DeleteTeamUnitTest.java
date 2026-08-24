package com.matchlog.be.service.team;

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

import com.matchlog.be.domain.player.Player;
import com.matchlog.be.domain.team.Team;
import com.matchlog.be.exception.CustomException;
import com.matchlog.be.exception.constant.CommonErrorCode;
import com.matchlog.be.exception.constant.TeamErrorCode;
import com.matchlog.be.repository.ParticipationRepository;
import com.matchlog.be.repository.TeamRepository;
import com.matchlog.be.service.player.PlayerService;
import java.util.Optional;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
class DeleteTeamUnitTest {

    private static final Long USER_ID = 1L;

    @Mock private TeamRepository teamRepository;
    @Mock private ParticipationRepository participationRepository;
    @Mock private PlayerService playerService;
    @Mock private TeamAuthorizationService teamAuthorizationService;
    @InjectMocks private TeamService teamService;

    @Test
    void MANAGER면_팀_삭제에_성공한다() {
        Player player = Player.builder().id(9L).build();
        Team team = Team.builder().id(1L).name("FC 한강불사조").inviteCode("HK4829").build();

        when(playerService.getCurrentPlayer(USER_ID)).thenReturn(player);
        when(teamRepository.findById(1L)).thenReturn(Optional.of(team));
        doNothing().when(teamAuthorizationService).requireManager(eq(1L), eq(9L), anyString());

        teamService.deleteTeam(USER_ID, 1L);

        verify(teamRepository, times(1)).delete(team);
    }

    @Test
    void 존재하지_않는_팀이면_TEAM_NOT_FOUND_예외가_발생하고_삭제하지_않는다() {
        Player player = Player.builder().id(9L).build();

        when(playerService.getCurrentPlayer(USER_ID)).thenReturn(player);
        when(teamRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> teamService.deleteTeam(USER_ID, 999L))
                .isInstanceOf(CustomException.class)
                .satisfies(
                        e ->
                                assertThat(((CustomException) e).getErrorCode())
                                        .isEqualTo(TeamErrorCode.TEAM_NOT_FOUND));

        verify(teamRepository, never()).delete(any(Team.class));
    }

    @Test
    void MANAGER가_아니면_FORBIDDEN_예외가_발생하고_삭제하지_않는다() {
        Player player = Player.builder().id(9L).build();
        Team team = Team.builder().id(1L).name("FC 한강불사조").inviteCode("HK4829").build();

        when(playerService.getCurrentPlayer(USER_ID)).thenReturn(player);
        when(teamRepository.findById(1L)).thenReturn(Optional.of(team));
        doThrow(new CustomException(CommonErrorCode.FORBIDDEN, "팀 삭제 권한이 없습니다. (MANAGER만 가능)"))
                .when(teamAuthorizationService)
                .requireManager(1L, 9L, "팀 삭제 권한이 없습니다. (MANAGER만 가능)");

        assertThatThrownBy(() -> teamService.deleteTeam(USER_ID, 1L))
                .isInstanceOf(CustomException.class)
                .satisfies(
                        e ->
                                assertThat(((CustomException) e).getErrorCode())
                                        .isEqualTo(CommonErrorCode.FORBIDDEN));

        verify(teamRepository, never()).delete(team);
    }
}
