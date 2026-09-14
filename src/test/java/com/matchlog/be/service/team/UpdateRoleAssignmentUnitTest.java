package com.matchlog.be.service.team;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import com.matchlog.be.domain.participation.Participation;
import com.matchlog.be.domain.player.Player;
import com.matchlog.be.domain.team.Team;
import com.matchlog.be.domain.team.TeamRoleAssignment;
import com.matchlog.be.domain.user.User;
import com.matchlog.be.dto.team.request.UpdateRoleAssignmentRequestDto;
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
class UpdateRoleAssignmentUnitTest {

    private static final Long USER_ID = 1L;

    @Mock private TeamRoleAssignmentRepository teamRoleAssignmentRepository;
    @Mock private ParticipationRepository participationRepository;
    @Mock private TeamRepository teamRepository;
    @Mock private PlayerService playerService;
    @Mock private TeamAuthorizationService teamAuthorizationService;
    @InjectMocks private TeamRoleAssignmentService teamRoleAssignmentService;

    @Test
    void MANAGER면_같은_선수를_여러_역할에_동시_지정할_수_있다() {
        Long teamId = 1L;
        Player manager = Player.builder().id(9L).build();
        Team team = Team.builder().id(teamId).build();
        User captainUser = User.builder().id(100L).name("임준혁").build();
        Player captainPlayer = Player.builder().id(9L).user(captainUser).build();
        Participation captainParticipation =
                Participation.builder().id(9L).player(captainPlayer).team(team).build();
        TeamRoleAssignment assignment = TeamRoleAssignment.builder().id(1L).team(team).build();
        UpdateRoleAssignmentRequestDto request =
                UpdateRoleAssignmentRequestDto.builder()
                        .captainParticipationId(9L)
                        .pkTakerParticipationId(9L)
                        .build();

        when(playerService.getCurrentPlayer(USER_ID)).thenReturn(manager);
        when(teamRepository.existsById(teamId)).thenReturn(true);
        doNothing().when(teamAuthorizationService).requireManager(eq(teamId), eq(9L), anyString());
        when(teamRoleAssignmentRepository.findByTeam_Id(teamId))
                .thenReturn(Optional.of(assignment));
        when(participationRepository.findById(9L)).thenReturn(Optional.of(captainParticipation));

        RoleAssignmentResponseDto response =
                teamRoleAssignmentService.updateRoleAssignment(USER_ID, teamId, request);

        assertThat(response.getCaptain().getParticipationId()).isEqualTo(9L);
        assertThat(response.getPkTaker().getParticipationId()).isEqualTo(9L);
    }

    @Test
    void null을_전달하면_해당_역할이_해제된다() {
        Long teamId = 1L;
        Player manager = Player.builder().id(9L).build();
        Team team = Team.builder().id(teamId).build();
        Player captainPlayer = Player.builder().id(9L).user(User.builder().id(100L).build()).build();
        Participation captainParticipation =
                Participation.builder().id(9L).player(captainPlayer).team(team).build();
        TeamRoleAssignment assignment =
                TeamRoleAssignment.builder()
                        .id(1L)
                        .team(team)
                        .captainParticipation(captainParticipation)
                        .build();
        UpdateRoleAssignmentRequestDto request =
                UpdateRoleAssignmentRequestDto.builder().captainParticipationId(null).build();

        when(playerService.getCurrentPlayer(USER_ID)).thenReturn(manager);
        when(teamRepository.existsById(teamId)).thenReturn(true);
        doNothing().when(teamAuthorizationService).requireManager(eq(teamId), eq(9L), anyString());
        when(teamRoleAssignmentRepository.findByTeam_Id(teamId))
                .thenReturn(Optional.of(assignment));

        RoleAssignmentResponseDto response =
                teamRoleAssignmentService.updateRoleAssignment(USER_ID, teamId, request);

        assertThat(response.getCaptain()).isNull();
    }

    @Test
    void MANAGER가_아니면_FORBIDDEN_예외가_발생한다() {
        Long teamId = 1L;
        Player requester = Player.builder().id(9L).build();
        UpdateRoleAssignmentRequestDto request = UpdateRoleAssignmentRequestDto.builder().build();

        when(playerService.getCurrentPlayer(USER_ID)).thenReturn(requester);
        when(teamRepository.existsById(teamId)).thenReturn(true);
        doThrow(new CustomException(CommonErrorCode.FORBIDDEN, "세트피스 역할 지정 권한이 없습니다. (MANAGER만 가능)"))
                .when(teamAuthorizationService)
                .requireManager(eq(teamId), eq(9L), anyString());

        assertThatThrownBy(
                        () ->
                                teamRoleAssignmentService.updateRoleAssignment(
                                        USER_ID, teamId, request))
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
        UpdateRoleAssignmentRequestDto request = UpdateRoleAssignmentRequestDto.builder().build();

        when(playerService.getCurrentPlayer(USER_ID)).thenReturn(requester);
        when(teamRepository.existsById(teamId)).thenReturn(false);

        assertThatThrownBy(
                        () ->
                                teamRoleAssignmentService.updateRoleAssignment(
                                        USER_ID, teamId, request))
                .isInstanceOf(CustomException.class)
                .satisfies(
                        e ->
                                assertThat(((CustomException) e).getErrorCode())
                                        .isEqualTo(TeamErrorCode.TEAM_NOT_FOUND));
    }

    @Test
    void 지정하려는_참가정보가_다른_팀_소속이면_MEMBER_NOT_FOUND_예외가_발생한다() {
        Long teamId = 1L;
        Player manager = Player.builder().id(9L).build();
        Team team = Team.builder().id(teamId).build();
        Team otherTeam = Team.builder().id(2L).build();
        Player otherPlayer = Player.builder().id(20L).build();
        Participation otherTeamParticipation =
                Participation.builder().id(20L).player(otherPlayer).team(otherTeam).build();
        TeamRoleAssignment assignment = TeamRoleAssignment.builder().id(1L).team(team).build();
        UpdateRoleAssignmentRequestDto request =
                UpdateRoleAssignmentRequestDto.builder().captainParticipationId(20L).build();

        when(playerService.getCurrentPlayer(USER_ID)).thenReturn(manager);
        when(teamRepository.existsById(teamId)).thenReturn(true);
        doNothing().when(teamAuthorizationService).requireManager(eq(teamId), eq(9L), anyString());
        when(teamRoleAssignmentRepository.findByTeam_Id(teamId))
                .thenReturn(Optional.of(assignment));
        when(participationRepository.findById(20L)).thenReturn(Optional.of(otherTeamParticipation));

        assertThatThrownBy(
                        () ->
                                teamRoleAssignmentService.updateRoleAssignment(
                                        USER_ID, teamId, request))
                .isInstanceOf(CustomException.class)
                .satisfies(
                        e ->
                                assertThat(((CustomException) e).getErrorCode())
                                        .isEqualTo(TeamErrorCode.MEMBER_NOT_FOUND));
    }

    @Test
    void 존재하지_않는_참가정보면_MEMBER_NOT_FOUND_예외가_발생한다() {
        Long teamId = 1L;
        Player manager = Player.builder().id(9L).build();
        Team team = Team.builder().id(teamId).build();
        TeamRoleAssignment assignment = TeamRoleAssignment.builder().id(1L).team(team).build();
        UpdateRoleAssignmentRequestDto request =
                UpdateRoleAssignmentRequestDto.builder().captainParticipationId(999L).build();

        when(playerService.getCurrentPlayer(USER_ID)).thenReturn(manager);
        when(teamRepository.existsById(teamId)).thenReturn(true);
        doNothing().when(teamAuthorizationService).requireManager(eq(teamId), eq(9L), anyString());
        when(teamRoleAssignmentRepository.findByTeam_Id(teamId))
                .thenReturn(Optional.of(assignment));
        when(participationRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(
                        () ->
                                teamRoleAssignmentService.updateRoleAssignment(
                                        USER_ID, teamId, request))
                .isInstanceOf(CustomException.class)
                .satisfies(
                        e ->
                                assertThat(((CustomException) e).getErrorCode())
                                        .isEqualTo(TeamErrorCode.MEMBER_NOT_FOUND));
    }
}
