package com.matchlog.be.service.team;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.matchlog.be.constant.participation.ParticipationRole;
import com.matchlog.be.domain.participation.Participation;
import com.matchlog.be.domain.player.Player;
import com.matchlog.be.domain.team.Team;
import com.matchlog.be.dto.team.request.UpdateTeamRequestDto;
import com.matchlog.be.dto.team.response.UpdateTeamResponseDto;
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
class UpdateTeamUnitTest {

    private static final Long USER_ID = 1L;

    @Mock private TeamRepository teamRepository;
    @Mock private ParticipationRepository participationRepository;
    @Mock private PlayerService playerService;
    @InjectMocks private TeamService teamService;

    @Test
    void MANAGER면_팀_정보_수정에_성공한다() {
        Player player = Player.builder().id(9L).build();
        Team team =
                Team.builder()
                        .id(1L)
                        .name("FC 한강불사조")
                        .region("서울 마포구")
                        .inviteCode("HK4829")
                        .build();
        Participation participation = Participation.create(player, team, ParticipationRole.MANAGER);
        UpdateTeamRequestDto request = UpdateTeamRequestDto.builder().name("한강불사조 FC").build();

        when(playerService.getCurrentPlayer(USER_ID)).thenReturn(player);
        when(teamRepository.findById(1L)).thenReturn(Optional.of(team));
        when(participationRepository.findByTeam_IdAndPlayer_Id(1L, 9L))
                .thenReturn(Optional.of(participation));

        UpdateTeamResponseDto response = teamService.updateTeam(USER_ID, 1L, request);

        assertThat(response.getTeamId()).isEqualTo(1L);
        assertThat(response.getName()).isEqualTo("한강불사조 FC");
        assertThat(team.getRegion()).isEqualTo("서울 마포구");
    }

    @Test
    void 존재하지_않는_팀이면_TEAM_NOT_FOUND_예외가_발생한다() {
        Player player = Player.builder().id(9L).build();
        UpdateTeamRequestDto request = UpdateTeamRequestDto.builder().name("한강불사조 FC").build();

        when(playerService.getCurrentPlayer(USER_ID)).thenReturn(player);
        when(teamRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> teamService.updateTeam(USER_ID, 999L, request))
                .isInstanceOf(CustomException.class)
                .satisfies(
                        e ->
                                assertThat(((CustomException) e).getErrorCode())
                                        .isEqualTo(TeamErrorCode.TEAM_NOT_FOUND));
    }

    @Test
    void MANAGER가_아니면_FORBIDDEN_예외가_발생한다() {
        Player player = Player.builder().id(9L).build();
        Team team = Team.builder().id(1L).name("FC 한강불사조").inviteCode("HK4829").build();
        Participation participation = Participation.create(player, team, ParticipationRole.PLAYER);
        UpdateTeamRequestDto request = UpdateTeamRequestDto.builder().name("한강불사조 FC").build();

        when(playerService.getCurrentPlayer(USER_ID)).thenReturn(player);
        when(teamRepository.findById(1L)).thenReturn(Optional.of(team));
        when(participationRepository.findByTeam_IdAndPlayer_Id(1L, 9L))
                .thenReturn(Optional.of(participation));

        assertThatThrownBy(() -> teamService.updateTeam(USER_ID, 1L, request))
                .isInstanceOf(CustomException.class)
                .satisfies(
                        e ->
                                assertThat(((CustomException) e).getErrorCode())
                                        .isEqualTo(CommonErrorCode.FORBIDDEN));

        assertThat(team.getName()).isEqualTo("FC 한강불사조");
    }
}
