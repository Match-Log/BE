package com.matchlog.be.service.participation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.matchlog.be.constant.participation.ParticipationRole;
import com.matchlog.be.domain.participation.Participation;
import com.matchlog.be.domain.player.Player;
import com.matchlog.be.domain.team.Team;
import com.matchlog.be.dto.participation.request.UpdateParticipationRequestDto;
import com.matchlog.be.dto.participation.response.UpdateParticipationResponseDto;
import com.matchlog.be.exception.CustomException;
import com.matchlog.be.exception.constant.CommonErrorCode;
import com.matchlog.be.exception.constant.ParticipationErrorCode;
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
class UpdateParticipationUnitTest {

    private static final Long USER_ID = 1L;
    private static final Long TEAM_ID = 1L;

    @Mock private ParticipationRepository participationRepository;
    @Mock private TeamRepository teamRepository;
    @Mock private PlayerService playerService;
    @Mock private TeamAuthorizationService teamAuthorizationService;
    @InjectMocks private ParticipationService participationService;

    @Test
    void MANAGER면_등번호와_포지션_수정에_성공한다() {
        Player manager = Player.builder().id(9L).build();
        Player target = Player.builder().id(10L).build();
        Team team = Team.builder().id(TEAM_ID).build();
        Participation managerParticipation =
                Participation.create(manager, team, ParticipationRole.MANAGER);
        Participation targetParticipation =
                Participation.create(target, team, ParticipationRole.PLAYER);
        UpdateParticipationRequestDto request =
                UpdateParticipationRequestDto.builder().number(4).mainPosition("CB").build();

        when(playerService.getCurrentPlayer(USER_ID)).thenReturn(manager);
        when(participationRepository.findByTeam_IdAndPlayer_Id(TEAM_ID, 9L))
                .thenReturn(Optional.of(managerParticipation));
        when(participationRepository.findByTeam_IdAndPlayer_Id(TEAM_ID, 10L))
                .thenReturn(Optional.of(targetParticipation));

        UpdateParticipationResponseDto response =
                participationService.updateParticipation(USER_ID, TEAM_ID, 10L, request);

        assertThat(response.getNumber()).isEqualTo(4);
        assertThat(response.getMainPosition()).isEqualTo("CB");
        assertThat(response.getRole()).isEqualTo(ParticipationRole.PLAYER);
    }

    @Test
    void MANAGER면_역할_변경에_성공한다() {
        Player manager = Player.builder().id(9L).build();
        Player target = Player.builder().id(10L).build();
        Team team = Team.builder().id(TEAM_ID).build();
        Participation managerParticipation =
                Participation.create(manager, team, ParticipationRole.MANAGER);
        Participation targetParticipation =
                Participation.create(target, team, ParticipationRole.PLAYER);
        UpdateParticipationRequestDto request =
                UpdateParticipationRequestDto.builder().role(ParticipationRole.MANAGER).build();

        when(playerService.getCurrentPlayer(USER_ID)).thenReturn(manager);
        when(participationRepository.findByTeam_IdAndPlayer_Id(TEAM_ID, 9L))
                .thenReturn(Optional.of(managerParticipation));
        when(participationRepository.findByTeam_IdAndPlayer_Id(TEAM_ID, 10L))
                .thenReturn(Optional.of(targetParticipation));

        UpdateParticipationResponseDto response =
                participationService.updateParticipation(USER_ID, TEAM_ID, 10L, request);

        assertThat(response.getRole()).isEqualTo(ParticipationRole.MANAGER);
    }

    @Test
    void 요청자가_팀_소속이_아니면_FORBIDDEN_예외가_발생한다() {
        Player requester = Player.builder().id(9L).build();
        UpdateParticipationRequestDto request =
                UpdateParticipationRequestDto.builder().number(4).build();

        when(playerService.getCurrentPlayer(USER_ID)).thenReturn(requester);
        when(participationRepository.findByTeam_IdAndPlayer_Id(TEAM_ID, 9L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(
                        () ->
                                participationService.updateParticipation(
                                        USER_ID, TEAM_ID, 10L, request))
                .isInstanceOf(CustomException.class)
                .satisfies(
                        e ->
                                assertThat(((CustomException) e).getErrorCode())
                                        .isEqualTo(CommonErrorCode.FORBIDDEN));
    }

    @Test
    void MANAGER가_아니면_FORBIDDEN_예외가_발생한다() {
        Player requester = Player.builder().id(9L).build();
        Team team = Team.builder().id(TEAM_ID).build();
        Participation requesterParticipation =
                Participation.create(requester, team, ParticipationRole.PLAYER);
        UpdateParticipationRequestDto request =
                UpdateParticipationRequestDto.builder().number(4).build();

        when(playerService.getCurrentPlayer(USER_ID)).thenReturn(requester);
        when(participationRepository.findByTeam_IdAndPlayer_Id(TEAM_ID, 9L))
                .thenReturn(Optional.of(requesterParticipation));

        assertThatThrownBy(
                        () ->
                                participationService.updateParticipation(
                                        USER_ID, TEAM_ID, 10L, request))
                .isInstanceOf(CustomException.class)
                .satisfies(
                        e ->
                                assertThat(((CustomException) e).getErrorCode())
                                        .isEqualTo(CommonErrorCode.FORBIDDEN));
    }

    @Test
    void 대상_참가정보가_없으면_PARTICIPATION_NOT_FOUND_예외가_발생한다() {
        Player manager = Player.builder().id(9L).build();
        Team team = Team.builder().id(TEAM_ID).build();
        Participation managerParticipation =
                Participation.create(manager, team, ParticipationRole.MANAGER);
        UpdateParticipationRequestDto request =
                UpdateParticipationRequestDto.builder().number(4).build();

        when(playerService.getCurrentPlayer(USER_ID)).thenReturn(manager);
        when(participationRepository.findByTeam_IdAndPlayer_Id(TEAM_ID, 9L))
                .thenReturn(Optional.of(managerParticipation));
        when(participationRepository.findByTeam_IdAndPlayer_Id(TEAM_ID, 999L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(
                        () ->
                                participationService.updateParticipation(
                                        USER_ID, TEAM_ID, 999L, request))
                .isInstanceOf(CustomException.class)
                .satisfies(
                        e ->
                                assertThat(((CustomException) e).getErrorCode())
                                        .isEqualTo(ParticipationErrorCode.PARTICIPATION_NOT_FOUND));
    }

    @Test
    void 본인의_역할을_변경하려_하면_FORBIDDEN_예외가_발생한다() {
        Player manager = Player.builder().id(9L).build();
        Team team = Team.builder().id(TEAM_ID).build();
        Participation managerParticipation =
                Participation.create(manager, team, ParticipationRole.MANAGER);
        UpdateParticipationRequestDto request =
                UpdateParticipationRequestDto.builder().role(ParticipationRole.PLAYER).build();

        when(playerService.getCurrentPlayer(USER_ID)).thenReturn(manager);
        when(participationRepository.findByTeam_IdAndPlayer_Id(TEAM_ID, 9L))
                .thenReturn(Optional.of(managerParticipation));

        assertThatThrownBy(
                        () ->
                                participationService.updateParticipation(
                                        USER_ID, TEAM_ID, 9L, request))
                .isInstanceOf(CustomException.class)
                .satisfies(
                        e ->
                                assertThat(((CustomException) e).getErrorCode())
                                        .isEqualTo(CommonErrorCode.FORBIDDEN));
    }
}
