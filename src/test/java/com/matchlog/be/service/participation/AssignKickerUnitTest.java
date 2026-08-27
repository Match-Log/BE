package com.matchlog.be.service.participation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

import com.matchlog.be.constant.participation.ParticipationRole;
import com.matchlog.be.domain.participation.Participation;
import com.matchlog.be.domain.player.Player;
import com.matchlog.be.domain.team.Team;
import com.matchlog.be.dto.participation.request.AssignKickerRequestDto;
import com.matchlog.be.dto.participation.response.AssignKickerResponseDto;
import com.matchlog.be.exception.CustomException;
import com.matchlog.be.exception.constant.TeamErrorCode;
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
class AssignKickerUnitTest {

    private static final Long USER_ID = 1L;
    private static final Long TEAM_ID = 1L;

    @Mock private ParticipationRepository participationRepository;
    @Mock private TeamRepository teamRepository;
    @Mock private PlayerService playerService;
    @Mock private TeamAuthorizationService teamAuthorizationService;
    @InjectMocks private ParticipationService participationService;

    @Test
    void 아직_주장이_없는_팀이면_주장_지정에_성공한다() {
        Player manager = Player.builder().id(9L).build();
        Player target = Player.builder().id(10L).build();
        Team team = Team.builder().id(TEAM_ID).build();
        Participation targetParticipation =
                Participation.create(target, team, ParticipationRole.PLAYER);
        AssignKickerRequestDto request = AssignKickerRequestDto.builder().isCaptain(true).build();

        when(playerService.getCurrentPlayer(USER_ID)).thenReturn(manager);
        doNothing().when(teamAuthorizationService).requireManager(eq(TEAM_ID), eq(9L), anyString());
        when(participationRepository.findByTeam_IdAndPlayer_Id(TEAM_ID, 10L))
                .thenReturn(Optional.of(targetParticipation));
        when(participationRepository.findCurrentCaptainByTeamId(TEAM_ID))
                .thenReturn(Optional.empty());

        AssignKickerResponseDto response =
                participationService.assignKicker(USER_ID, TEAM_ID, 10L, request);

        assertThat(response.isCaptain()).isTrue();
        assertThat(targetParticipation.isCaptain()).isTrue();
    }

    @Test
    void 이미_본인이_주장이면_재지정해도_성공한다() {
        Player manager = Player.builder().id(9L).build();
        Player target = Player.builder().id(10L).build();
        Team team = Team.builder().id(TEAM_ID).build();
        Participation targetParticipation =
                Participation.create(target, team, ParticipationRole.PLAYER);
        targetParticipation.assignKickerRoles(true, false, false, false, false, false);
        AssignKickerRequestDto request = AssignKickerRequestDto.builder().isCaptain(true).build();

        when(playerService.getCurrentPlayer(USER_ID)).thenReturn(manager);
        doNothing().when(teamAuthorizationService).requireManager(eq(TEAM_ID), eq(9L), anyString());
        when(participationRepository.findByTeam_IdAndPlayer_Id(TEAM_ID, 10L))
                .thenReturn(Optional.of(targetParticipation));
        when(participationRepository.findCurrentCaptainByTeamId(TEAM_ID))
                .thenReturn(Optional.of(targetParticipation));

        AssignKickerResponseDto response =
                participationService.assignKicker(USER_ID, TEAM_ID, 10L, request);

        assertThat(response.isCaptain()).isTrue();
    }

    @Test
    void 이미_다른_선수가_주장이면_CAPTAIN_ALREADY_ASSIGNED_예외가_발생한다() {
        Player manager = Player.builder().id(9L).build();
        Player target = Player.builder().id(10L).build();
        Player existingCaptainPlayer = Player.builder().id(11L).build();
        Team team = Team.builder().id(TEAM_ID).build();
        Participation targetParticipation =
                Participation.create(target, team, ParticipationRole.PLAYER);
        Participation existingCaptainParticipation =
                Participation.create(existingCaptainPlayer, team, ParticipationRole.PLAYER);
        existingCaptainParticipation.assignKickerRoles(true, false, false, false, false, false);
        AssignKickerRequestDto request = AssignKickerRequestDto.builder().isCaptain(true).build();

        when(playerService.getCurrentPlayer(USER_ID)).thenReturn(manager);
        doNothing().when(teamAuthorizationService).requireManager(eq(TEAM_ID), eq(9L), anyString());
        when(participationRepository.findByTeam_IdAndPlayer_Id(TEAM_ID, 10L))
                .thenReturn(Optional.of(targetParticipation));
        when(participationRepository.findCurrentCaptainByTeamId(TEAM_ID))
                .thenReturn(Optional.of(existingCaptainParticipation));

        assertThatThrownBy(() -> participationService.assignKicker(USER_ID, TEAM_ID, 10L, request))
                .isInstanceOf(CustomException.class)
                .satisfies(
                        e ->
                                assertThat(((CustomException) e).getErrorCode())
                                        .isEqualTo(TeamErrorCode.CAPTAIN_ALREADY_ASSIGNED));

        assertThat(targetParticipation.isCaptain()).isFalse();
    }

    @Test
    void 이미_다른_선수가_PK키커면_PK_TAKER_ALREADY_ASSIGNED_예외가_발생한다() {
        Player manager = Player.builder().id(9L).build();
        Player target = Player.builder().id(10L).build();
        Player existingPkTakerPlayer = Player.builder().id(11L).build();
        Team team = Team.builder().id(TEAM_ID).build();
        Participation targetParticipation =
                Participation.create(target, team, ParticipationRole.PLAYER);
        Participation existingPkTakerParticipation =
                Participation.create(existingPkTakerPlayer, team, ParticipationRole.PLAYER);
        existingPkTakerParticipation.assignKickerRoles(false, true, false, false, false, false);
        AssignKickerRequestDto request = AssignKickerRequestDto.builder().isPkTaker(true).build();

        when(playerService.getCurrentPlayer(USER_ID)).thenReturn(manager);
        doNothing().when(teamAuthorizationService).requireManager(eq(TEAM_ID), eq(9L), anyString());
        when(participationRepository.findByTeam_IdAndPlayer_Id(TEAM_ID, 10L))
                .thenReturn(Optional.of(targetParticipation));
        when(participationRepository.findCurrentPkTakerByTeamId(TEAM_ID))
                .thenReturn(Optional.of(existingPkTakerParticipation));

        assertThatThrownBy(() -> participationService.assignKicker(USER_ID, TEAM_ID, 10L, request))
                .isInstanceOf(CustomException.class)
                .satisfies(
                        e ->
                                assertThat(((CustomException) e).getErrorCode())
                                        .isEqualTo(TeamErrorCode.PK_TAKER_ALREADY_ASSIGNED));
    }

    @Test
    void 대상_참가정보가_없으면_MEMBER_NOT_FOUND_예외가_발생한다() {
        Player manager = Player.builder().id(9L).build();
        AssignKickerRequestDto request = AssignKickerRequestDto.builder().isCaptain(true).build();

        when(playerService.getCurrentPlayer(USER_ID)).thenReturn(manager);
        doNothing().when(teamAuthorizationService).requireManager(eq(TEAM_ID), eq(9L), anyString());
        when(participationRepository.findByTeam_IdAndPlayer_Id(TEAM_ID, 999L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> participationService.assignKicker(USER_ID, TEAM_ID, 999L, request))
                .isInstanceOf(CustomException.class)
                .satisfies(
                        e ->
                                assertThat(((CustomException) e).getErrorCode())
                                        .isEqualTo(TeamErrorCode.MEMBER_NOT_FOUND));
    }

    @Test
    void 요청에_없는_필드는_기존_값을_유지한다() {
        Player manager = Player.builder().id(9L).build();
        Player target = Player.builder().id(10L).build();
        Team team = Team.builder().id(TEAM_ID).build();
        Participation targetParticipation =
                Participation.create(target, team, ParticipationRole.PLAYER);
        targetParticipation.assignKickerRoles(false, false, true, false, false, false);
        AssignKickerRequestDto request = AssignKickerRequestDto.builder().isCaptain(true).build();

        when(playerService.getCurrentPlayer(USER_ID)).thenReturn(manager);
        doNothing().when(teamAuthorizationService).requireManager(eq(TEAM_ID), eq(9L), anyString());
        when(participationRepository.findByTeam_IdAndPlayer_Id(TEAM_ID, 10L))
                .thenReturn(Optional.of(targetParticipation));
        when(participationRepository.findCurrentCaptainByTeamId(TEAM_ID))
                .thenReturn(Optional.empty());

        AssignKickerResponseDto response =
                participationService.assignKicker(USER_ID, TEAM_ID, 10L, request);

        assertThat(response.isCaptain()).isTrue();
        assertThat(response.isFkRight()).isTrue();
    }
}
