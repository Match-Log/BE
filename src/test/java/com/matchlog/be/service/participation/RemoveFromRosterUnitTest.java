package com.matchlog.be.service.participation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.matchlog.be.constant.participation.ParticipationRole;
import com.matchlog.be.domain.participation.Participation;
import com.matchlog.be.domain.player.Player;
import com.matchlog.be.domain.team.Team;
import com.matchlog.be.exception.CustomException;
import com.matchlog.be.exception.constant.CommonErrorCode;
import com.matchlog.be.exception.constant.ParticipationErrorCode;
import com.matchlog.be.exception.constant.TeamErrorCode;
import com.matchlog.be.repository.ParticipationRepository;
import com.matchlog.be.repository.TeamRepository;
import com.matchlog.be.service.player.PlayerService;
import com.matchlog.be.service.team.TeamAuthorizationService;
import java.util.Optional;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
class RemoveFromRosterUnitTest {

    private static final Long USER_ID = 1L;
    private static final Long TEAM_ID = 1L;

    @Mock private ParticipationRepository participationRepository;
    @Mock private TeamRepository teamRepository;
    @Mock private PlayerService playerService;
    @Mock private TeamAuthorizationService teamAuthorizationService;
    @InjectMocks private ParticipationService participationService;

    @Test
    void MANAGER가_일반_PLAYER를_제외하면_성공한다() {
        Player manager = Player.builder().id(9L).build();
        Player target = Player.builder().id(10L).build();
        Team team = Team.builder().id(TEAM_ID).build();
        Participation targetParticipation =
                Participation.create(target, team, ParticipationRole.PLAYER);

        when(playerService.getCurrentPlayer(USER_ID)).thenReturn(manager);
        doNothing().when(teamAuthorizationService).requireManager(eq(TEAM_ID), eq(9L), anyString());
        when(participationRepository.findByTeam_IdAndPlayer_Id(TEAM_ID, 10L))
                .thenReturn(Optional.of(targetParticipation));

        participationService.removeFromRoster(USER_ID, TEAM_ID, 10L);

        verify(participationRepository, times(1)).delete(targetParticipation);
    }

    @Test
    void MANAGER가_아니면_FORBIDDEN_예외가_발생한다() {
        Player requester = Player.builder().id(9L).build();

        when(playerService.getCurrentPlayer(USER_ID)).thenReturn(requester);
        doThrow(new CustomException(CommonErrorCode.FORBIDDEN, "팀원 제외 권한이 없습니다. (MANAGER만 가능)"))
                .when(teamAuthorizationService)
                .requireManager(TEAM_ID, 9L, "팀원 제외 권한이 없습니다. (MANAGER만 가능)");

        assertThatThrownBy(() -> participationService.removeFromRoster(USER_ID, TEAM_ID, 10L))
                .isInstanceOf(CustomException.class)
                .satisfies(
                        e ->
                                assertThat(((CustomException) e).getErrorCode())
                                        .isEqualTo(CommonErrorCode.FORBIDDEN));

        verify(participationRepository, never()).delete(ArgumentMatchers.any(Participation.class));
    }

    @Test
    void 대상_참가정보가_없으면_PARTICIPATION_NOT_FOUND_예외가_발생한다() {
        Player manager = Player.builder().id(9L).build();

        when(playerService.getCurrentPlayer(USER_ID)).thenReturn(manager);
        doNothing().when(teamAuthorizationService).requireManager(eq(TEAM_ID), eq(9L), anyString());
        when(participationRepository.findByTeam_IdAndPlayer_Id(TEAM_ID, 999L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> participationService.removeFromRoster(USER_ID, TEAM_ID, 999L))
                .isInstanceOf(CustomException.class)
                .satisfies(
                        e ->
                                assertThat(((CustomException) e).getErrorCode())
                                        .isEqualTo(ParticipationErrorCode.PARTICIPATION_NOT_FOUND));
    }

    @Test
    void 마지막_MANAGER를_제외하려_하면_LAST_MANAGER_CANNOT_BE_REMOVED_예외가_발생한다() {
        Player manager = Player.builder().id(9L).build();
        Team team = Team.builder().id(TEAM_ID).build();
        Participation managerParticipation =
                Participation.create(manager, team, ParticipationRole.MANAGER);

        when(playerService.getCurrentPlayer(USER_ID)).thenReturn(manager);
        doNothing().when(teamAuthorizationService).requireManager(eq(TEAM_ID), eq(9L), anyString());
        when(participationRepository.findByTeam_IdAndPlayer_Id(TEAM_ID, 9L))
                .thenReturn(Optional.of(managerParticipation));
        when(participationRepository.countByTeam_IdAndRole(TEAM_ID, ParticipationRole.MANAGER))
                .thenReturn(1L);

        assertThatThrownBy(() -> participationService.removeFromRoster(USER_ID, TEAM_ID, 9L))
                .isInstanceOf(CustomException.class)
                .satisfies(
                        e ->
                                assertThat(((CustomException) e).getErrorCode())
                                        .isEqualTo(TeamErrorCode.LAST_MANAGER_CANNOT_BE_REMOVED));

        verify(participationRepository, never()).delete(ArgumentMatchers.any(Participation.class));
    }
}
