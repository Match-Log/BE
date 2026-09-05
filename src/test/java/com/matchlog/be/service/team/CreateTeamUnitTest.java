package com.matchlog.be.service.team;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.matchlog.be.constant.participation.ParticipationRole;
import com.matchlog.be.domain.participation.Participation;
import com.matchlog.be.domain.player.Player;
import com.matchlog.be.domain.team.Team;
import com.matchlog.be.domain.user.User;
import com.matchlog.be.dto.team.request.CreateTeamRequestDto;
import com.matchlog.be.dto.team.response.CreateTeamResponseDto;
import com.matchlog.be.exception.CustomException;
import com.matchlog.be.exception.constant.TeamErrorCode;
import com.matchlog.be.repository.ParticipationRepository;
import com.matchlog.be.repository.TeamRepository;
import com.matchlog.be.service.player.PlayerService;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
class CreateTeamUnitTest {

    @Mock private TeamRepository teamRepository;
    @Mock private ParticipationRepository participationRepository;
    @Mock private PlayerService playerService;
    @InjectMocks private TeamService teamService;

    @Test
    void 팀_생성에_성공하면_생성자가_MANAGER로_함께_등록된다() {
        Long userId = 1L;
        User user = User.builder().id(userId).email("user@example.com").name("임준혁").build();
        Player creator = Player.builder().id(10L).user(user).build();
        CreateTeamRequestDto request =
                CreateTeamRequestDto.builder()
                        .name("FC 한강불사조")
                        .region("서울 마포구")
                        .foundedYear(2020)
                        .homeGround("한강공원 풋살장")
                        .build();

        when(playerService.getCurrentPlayer(userId)).thenReturn(creator);
        when(teamRepository.existsByInviteCode(anyString())).thenReturn(false);
        when(teamRepository.save(any(Team.class)))
                .thenAnswer(
                        invocation -> {
                            Team saved = invocation.getArgument(0);
                            return Team.builder()
                                    .id(1L)
                                    .name(saved.getName())
                                    .teamImage(saved.getTeamImage())
                                    .region(saved.getRegion())
                                    .foundedYear(saved.getFoundedYear())
                                    .homeGround(saved.getHomeGround())
                                    .inviteCode(saved.getInviteCode())
                                    .build();
                        });

        CreateTeamResponseDto response = teamService.createTeam(userId, request);

        assertThat(response.getTeamId()).isEqualTo(1L);
        assertThat(response.getName()).isEqualTo("FC 한강불사조");
        assertThat(response.getInviteCode()).hasSize(6);

        ArgumentCaptor<Participation> captor = ArgumentCaptor.forClass(Participation.class);
        verify(participationRepository, times(1)).save(captor.capture());
        assertThat(captor.getValue().getRole()).isEqualTo(ParticipationRole.MANAGER);
        assertThat(captor.getValue().getPlayer()).isEqualTo(creator);
    }

    @Test
    void 생성된_초대코드가_이미_존재하면_충돌하지_않을_때까지_재시도한다() {
        Long userId = 1L;
        Player creator = Player.builder().id(10L).build();
        CreateTeamRequestDto request = CreateTeamRequestDto.builder().name("FC 한강불사조").build();

        when(playerService.getCurrentPlayer(userId)).thenReturn(creator);
        when(teamRepository.existsByInviteCode(anyString())).thenReturn(true, true, false);
        when(teamRepository.save(any(Team.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        teamService.createTeam(userId, request);

        verify(teamRepository, times(3)).existsByInviteCode(anyString());
    }

    @Test
    void 이미_2개_팀에_소속되어_있으면_TEAM_LIMIT_EXCEEDED_예외가_발생한다() {
        Long userId = 1L;
        Player creator = Player.builder().id(10L).build();
        CreateTeamRequestDto request = CreateTeamRequestDto.builder().name("FC 한강불사조").build();

        when(playerService.getCurrentPlayer(userId)).thenReturn(creator);
        when(participationRepository.countByPlayer_Id(10L)).thenReturn(2L);

        assertThatThrownBy(() -> teamService.createTeam(userId, request))
                .isInstanceOf(CustomException.class)
                .satisfies(
                        e ->
                                assertThat(((CustomException) e).getErrorCode())
                                        .isEqualTo(TeamErrorCode.TEAM_LIMIT_EXCEEDED));

        verify(teamRepository, never()).save(any(Team.class));
        verify(participationRepository, never()).save(any(Participation.class));
    }

    @Test
    void 재시도_횟수를_초과하면_INTERNAL_SERVER_ERROR_예외가_발생하고_저장하지_않는다() {
        Long userId = 1L;
        Player creator = Player.builder().id(10L).build();
        CreateTeamRequestDto request = CreateTeamRequestDto.builder().name("FC 한강불사조").build();

        when(playerService.getCurrentPlayer(userId)).thenReturn(creator);
        when(teamRepository.existsByInviteCode(anyString())).thenReturn(true);

        org.junit.jupiter.api.Assertions.assertThrows(
                com.matchlog.be.exception.CustomException.class,
                () -> teamService.createTeam(userId, request));

        verify(teamRepository, never()).save(any(Team.class));
        verify(participationRepository, never()).save(any(Participation.class));
    }
}
