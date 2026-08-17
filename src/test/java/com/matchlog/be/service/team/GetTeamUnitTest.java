package com.matchlog.be.service.team;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.matchlog.be.domain.player.Player;
import com.matchlog.be.domain.team.Team;
import com.matchlog.be.dto.team.response.TeamResponseDto;
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
class GetTeamUnitTest {

    private static final Long USER_ID = 1L;

    @Mock private TeamRepository teamRepository;
    @Mock private ParticipationRepository participationRepository;
    @Mock private PlayerService playerService;
    @InjectMocks private TeamService teamService;

    @Test
    void 팀_소속_멤버면_팀_정보_조회에_성공한다() {
        Player player = Player.builder().id(9L).build();
        Team team = Team.builder().id(1L).name("FC 한강불사조").inviteCode("HK4829").build();

        when(playerService.getCurrentPlayer(USER_ID)).thenReturn(player);
        when(teamRepository.findById(1L)).thenReturn(Optional.of(team));
        when(participationRepository.existsByTeam_IdAndPlayer_Id(1L, 9L)).thenReturn(true);

        TeamResponseDto response = teamService.getTeam(USER_ID, 1L);

        assertThat(response.getTeamId()).isEqualTo(1L);
        assertThat(response.getName()).isEqualTo("FC 한강불사조");
        assertThat(response.getInviteCode()).isEqualTo("HK4829");
    }

    @Test
    void 존재하지_않는_팀이면_TEAM_NOT_FOUND_예외가_발생한다() {
        Player player = Player.builder().id(9L).build();

        when(playerService.getCurrentPlayer(USER_ID)).thenReturn(player);
        when(teamRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> teamService.getTeam(USER_ID, 999L))
                .isInstanceOf(CustomException.class)
                .satisfies(
                        e ->
                                assertThat(((CustomException) e).getErrorCode())
                                        .isEqualTo(TeamErrorCode.TEAM_NOT_FOUND));
    }

    @Test
    void 팀_소속_멤버가_아니면_FORBIDDEN_예외가_발생한다() {
        Player player = Player.builder().id(9L).build();
        Team team = Team.builder().id(1L).name("FC 한강불사조").inviteCode("HK4829").build();

        when(playerService.getCurrentPlayer(USER_ID)).thenReturn(player);
        when(teamRepository.findById(1L)).thenReturn(Optional.of(team));
        when(participationRepository.existsByTeam_IdAndPlayer_Id(1L, 9L)).thenReturn(false);

        assertThatThrownBy(() -> teamService.getTeam(USER_ID, 1L))
                .isInstanceOf(CustomException.class)
                .satisfies(
                        e ->
                                assertThat(((CustomException) e).getErrorCode())
                                        .isEqualTo(CommonErrorCode.FORBIDDEN));
    }
}
