package com.matchlog.be.service.team;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.matchlog.be.constant.participation.ParticipationRole;
import com.matchlog.be.domain.participation.Participation;
import com.matchlog.be.domain.player.Player;
import com.matchlog.be.domain.team.Team;
import com.matchlog.be.dto.team.response.MyTeamResponseDto;
import com.matchlog.be.repository.ParticipationRepository;
import com.matchlog.be.repository.TeamRepository;
import com.matchlog.be.service.player.PlayerService;
import java.util.List;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
class GetMyTeamsUnitTest {

    private static final Long USER_ID = 1L;

    @Mock private TeamRepository teamRepository;
    @Mock private ParticipationRepository participationRepository;
    @Mock private PlayerService playerService;
    @InjectMocks private TeamService teamService;

    @Test
    void 소속된_팀_목록과_역할을_함께_반환한다() {
        Player player = Player.builder().id(9L).build();
        Team teamA = Team.builder().id(1L).name("FC 한강불사조").inviteCode("HK4829").build();
        Team teamB = Team.builder().id(2L).name("마포 유나이티드").inviteCode("MP1234").build();
        Participation participationA =
                Participation.create(player, teamA, ParticipationRole.PLAYER);
        Participation participationB =
                Participation.create(player, teamB, ParticipationRole.MANAGER);

        when(playerService.getCurrentPlayer(USER_ID)).thenReturn(player);
        when(participationRepository.findMyTeamsByPlayerId(9L))
                .thenReturn(List.of(participationA, participationB));

        List<MyTeamResponseDto> response = teamService.getMyTeams(USER_ID);

        assertThat(response).hasSize(2);
        assertThat(response.get(0).getTeamId()).isEqualTo(1L);
        assertThat(response.get(0).getRole()).isEqualTo("PLAYER");
        assertThat(response.get(1).getTeamId()).isEqualTo(2L);
        assertThat(response.get(1).getRole()).isEqualTo("MANAGER");
    }

    @Test
    void 소속된_팀이_없으면_빈_목록을_반환한다() {
        Player player = Player.builder().id(9L).build();

        when(playerService.getCurrentPlayer(USER_ID)).thenReturn(player);
        when(participationRepository.findMyTeamsByPlayerId(9L)).thenReturn(List.of());

        List<MyTeamResponseDto> response = teamService.getMyTeams(USER_ID);

        assertThat(response).isEmpty();
    }
}
