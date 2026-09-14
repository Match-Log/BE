package com.matchlog.be.service.team;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import com.matchlog.be.domain.participation.Participation;
import com.matchlog.be.domain.player.Player;
import com.matchlog.be.domain.team.Team;
import com.matchlog.be.domain.team.TeamRoleAssignment;
import com.matchlog.be.domain.user.User;
import com.matchlog.be.dto.team.response.RoleAssignmentResponseDto;
import com.matchlog.be.exception.CustomException;
import com.matchlog.be.exception.constant.CommonErrorCode;
import com.matchlog.be.exception.constant.TeamErrorCode;
import com.matchlog.be.repository.ParticipationRepository;
import com.matchlog.be.repository.TeamRepository;
import com.matchlog.be.repository.TeamRoleAssignmentRepository;
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
class GetRoleAssignmentUnitTest {

    private static final Long USER_ID = 1L;

    @Mock private TeamRoleAssignmentRepository teamRoleAssignmentRepository;
    @Mock private ParticipationRepository participationRepository;
    @Mock private TeamRepository teamRepository;
    @Mock private PlayerService playerService;
    @Mock private TeamAuthorizationService teamAuthorizationService;
    @InjectMocks private TeamRoleAssignmentService teamRoleAssignmentService;

    @Test
    void 팀원이면_역할_배정_조회에_성공한다() {
        Long teamId = 1L;
        Player requester = Player.builder().id(9L).build();
        Team team = Team.builder().id(teamId).build();
        User captainUser = User.builder().id(100L).name("임준혁").build();
        Player captainPlayer = Player.builder().id(9L).user(captainUser).build();
        Participation captainParticipation =
                Participation.builder().id(9L).player(captainPlayer).team(team).build();
        TeamRoleAssignment assignment =
                TeamRoleAssignment.builder()
                        .id(1L)
                        .team(team)
                        .captainParticipation(captainParticipation)
                        .build();

        when(playerService.getCurrentPlayer(USER_ID)).thenReturn(requester);
        when(teamRepository.existsById(teamId)).thenReturn(true);
        doNothing().when(teamAuthorizationService).requireMember(teamId, 9L);
        when(teamRoleAssignmentRepository.findByTeam_Id(teamId))
                .thenReturn(Optional.of(assignment));

        RoleAssignmentResponseDto response =
                teamRoleAssignmentService.getRoleAssignment(USER_ID, teamId);

        assertThat(response.getTeamId()).isEqualTo(teamId);
        assertThat(response.getCaptain().getParticipationId()).isEqualTo(9L);
        assertThat(response.getCaptain().getName()).isEqualTo("임준혁");
        assertThat(response.getPkTaker()).isNull();
    }

    @Test
    void 팀_소속이_아니면_FORBIDDEN_예외가_발생한다() {
        Long teamId = 1L;
        Player requester = Player.builder().id(9L).build();

        when(playerService.getCurrentPlayer(USER_ID)).thenReturn(requester);
        when(teamRepository.existsById(teamId)).thenReturn(true);
        doThrow(new CustomException(CommonErrorCode.FORBIDDEN, "해당 팀에 접근 권한이 없습니다."))
                .when(teamAuthorizationService)
                .requireMember(eq(teamId), eq(9L));

        assertThatThrownBy(() -> teamRoleAssignmentService.getRoleAssignment(USER_ID, teamId))
                .isInstanceOf(CustomException.class)
                .satisfies(
                        e ->
                                assertThat(((CustomException) e).getErrorCode())
                                        .isEqualTo(CommonErrorCode.FORBIDDEN));
    }

    @Test
    void 존재하지_않는_팀이면_TEAM_NOT_FOUND_예외가_발생한다() {
        Long teamId = 1L;
        Player requester = Player.builder().id(9L).build();

        when(playerService.getCurrentPlayer(USER_ID)).thenReturn(requester);
        when(teamRepository.existsById(teamId)).thenReturn(false);

        assertThatThrownBy(() -> teamRoleAssignmentService.getRoleAssignment(USER_ID, teamId))
                .isInstanceOf(CustomException.class)
                .satisfies(
                        e ->
                                assertThat(((CustomException) e).getErrorCode())
                                        .isEqualTo(TeamErrorCode.TEAM_NOT_FOUND));
    }
}
