package com.matchlog.be.service.participation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.matchlog.be.constant.participation.ParticipationRole;
import com.matchlog.be.domain.participation.Participation;
import com.matchlog.be.domain.player.Player;
import com.matchlog.be.domain.team.Team;
import com.matchlog.be.dto.participation.request.JoinTeamRequestDto;
import com.matchlog.be.dto.participation.response.JoinTeamResponseDto;
import com.matchlog.be.exception.CustomException;
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
class JoinTeamByInviteCodeUnitTest {

    @Mock private ParticipationRepository participationRepository;
    @Mock private TeamRepository teamRepository;
    @Mock private PlayerService playerService;
    @InjectMocks private ParticipationService participationService;

    private static final Long USER_ID = 1L;

    @Test
    void 유효한_초대코드면_PLAYER_역할로_가입에_성공한다() {
        Player player = Player.builder().id(9L).build();
        Team team = Team.builder().id(1L).name("FC 한강불사조").inviteCode("HK4829").build();
        JoinTeamRequestDto request = JoinTeamRequestDto.builder().inviteCode("HK4829").build();

        when(playerService.getCurrentPlayer(USER_ID)).thenReturn(player);
        when(teamRepository.findByInviteCode("HK4829")).thenReturn(Optional.of(team));
        when(participationRepository.existsByTeam_IdAndPlayer_Id(1L, 9L)).thenReturn(false);
        when(participationRepository.countByPlayer_Id(9L)).thenReturn(0L);
        when(participationRepository.save(any(Participation.class)))
                .thenAnswer(
                        invocation -> {
                            Participation saved = invocation.getArgument(0);
                            return Participation.builder()
                                    .id(1L)
                                    .player(saved.getPlayer())
                                    .team(saved.getTeam())
                                    .role(saved.getRole())
                                    .joinedAt(saved.getJoinedAt())
                                    .updatedAt(saved.getUpdatedAt())
                                    .build();
                        });

        JoinTeamResponseDto response = participationService.joinTeamByInviteCode(USER_ID, request);

        assertThat(response.getTeamId()).isEqualTo(1L);
        assertThat(response.getPlayerId()).isEqualTo(9L);
        assertThat(response.getRole()).isEqualTo(ParticipationRole.PLAYER);
    }

    @Test
    void 존재하지_않는_초대코드면_INVITE_CODE_NOT_FOUND_예외가_발생한다() {
        JoinTeamRequestDto request = JoinTeamRequestDto.builder().inviteCode("ZZZZZZ").build();

        when(playerService.getCurrentPlayer(USER_ID)).thenReturn(Player.builder().id(9L).build());
        when(teamRepository.findByInviteCode("ZZZZZZ")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> participationService.joinTeamByInviteCode(USER_ID, request))
                .isInstanceOf(CustomException.class)
                .satisfies(
                        e ->
                                assertThat(((CustomException) e).getErrorCode())
                                        .isEqualTo(TeamErrorCode.INVITE_CODE_NOT_FOUND));
    }

    @Test
    void 이미_가입된_팀이면_ALREADY_JOINED_예외가_발생한다() {
        Player player = Player.builder().id(9L).build();
        Team team = Team.builder().id(1L).inviteCode("HK4829").build();
        JoinTeamRequestDto request = JoinTeamRequestDto.builder().inviteCode("HK4829").build();

        when(playerService.getCurrentPlayer(USER_ID)).thenReturn(player);
        when(teamRepository.findByInviteCode("HK4829")).thenReturn(Optional.of(team));
        when(participationRepository.existsByTeam_IdAndPlayer_Id(1L, 9L)).thenReturn(true);

        assertThatThrownBy(() -> participationService.joinTeamByInviteCode(USER_ID, request))
                .isInstanceOf(CustomException.class)
                .satisfies(
                        e ->
                                assertThat(((CustomException) e).getErrorCode())
                                        .isEqualTo(TeamErrorCode.ALREADY_JOINED));
    }

    @Test
    void 이미_2개_팀에_소속되어_있으면_TEAM_LIMIT_EXCEEDED_예외가_발생한다() {
        Player player = Player.builder().id(9L).build();
        Team team = Team.builder().id(1L).inviteCode("HK4829").build();
        JoinTeamRequestDto request = JoinTeamRequestDto.builder().inviteCode("HK4829").build();

        when(playerService.getCurrentPlayer(USER_ID)).thenReturn(player);
        when(teamRepository.findByInviteCode("HK4829")).thenReturn(Optional.of(team));
        when(participationRepository.existsByTeam_IdAndPlayer_Id(1L, 9L)).thenReturn(false);
        when(participationRepository.countByPlayer_Id(9L)).thenReturn(2L);

        assertThatThrownBy(() -> participationService.joinTeamByInviteCode(USER_ID, request))
                .isInstanceOf(CustomException.class)
                .satisfies(
                        e ->
                                assertThat(((CustomException) e).getErrorCode())
                                        .isEqualTo(TeamErrorCode.TEAM_LIMIT_EXCEEDED));
    }
}
