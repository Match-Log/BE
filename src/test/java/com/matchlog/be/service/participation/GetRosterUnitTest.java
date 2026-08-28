package com.matchlog.be.service.participation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.matchlog.be.constant.participation.ParticipationRole;
import com.matchlog.be.domain.participation.Participation;
import com.matchlog.be.domain.player.Player;
import com.matchlog.be.domain.team.Team;
import com.matchlog.be.domain.user.User;
import com.matchlog.be.dto.participation.response.RosterItemResponseDto;
import com.matchlog.be.exception.CustomException;
import com.matchlog.be.exception.constant.CommonErrorCode;
import com.matchlog.be.exception.constant.TeamErrorCode;
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
class GetRosterUnitTest {

    private static final Long USER_ID = 1L;

    @Mock private ParticipationRepository participationRepository;
    @Mock private TeamRepository teamRepository;
    @Mock private PlayerService playerService;
    @InjectMocks private ParticipationService participationService;

    @Test
    void 팀_소속_멤버면_로스터_조회에_성공한다() {
        Player requester = Player.builder().id(9L).build();
        User user = User.builder().id(1L).name("임준혁").build();
        Player rosterPlayer = Player.builder().id(9L).user(user).build();
        Team team = Team.builder().id(1L).name("FC 한강불사조").build();
        Participation participation =
                Participation.create(rosterPlayer, team, ParticipationRole.PLAYER);

        when(playerService.getCurrentPlayer(USER_ID)).thenReturn(requester);
        when(teamRepository.existsById(1L)).thenReturn(true);
        when(participationRepository.existsByTeam_IdAndPlayer_Id(1L, 9L)).thenReturn(true);
        when(participationRepository.findRosterByTeamId(1L)).thenReturn(List.of(participation));

        List<RosterItemResponseDto> response = participationService.getRoster(USER_ID, 1L);

        assertThat(response).hasSize(1);
        assertThat(response.get(0).getTeamId()).isEqualTo(1L);
        assertThat(response.get(0).getPlayerId()).isEqualTo(9L);
        assertThat(response.get(0).getName()).isEqualTo("임준혁");
    }

    @Test
    void 존재하지_않는_팀이면_TEAM_NOT_FOUND_예외가_발생한다() {
        Player requester = Player.builder().id(9L).build();

        when(playerService.getCurrentPlayer(USER_ID)).thenReturn(requester);
        when(teamRepository.existsById(999L)).thenReturn(false);

        assertThatThrownBy(() -> participationService.getRoster(USER_ID, 999L))
                .isInstanceOf(CustomException.class)
                .satisfies(
                        e ->
                                assertThat(((CustomException) e).getErrorCode())
                                        .isEqualTo(TeamErrorCode.TEAM_NOT_FOUND));
    }

    @Test
    void 팀_소속이_아니면_FORBIDDEN_예외가_발생한다() {
        Player requester = Player.builder().id(9L).build();

        when(playerService.getCurrentPlayer(USER_ID)).thenReturn(requester);
        when(teamRepository.existsById(1L)).thenReturn(true);
        when(participationRepository.existsByTeam_IdAndPlayer_Id(1L, 9L)).thenReturn(false);

        assertThatThrownBy(() -> participationService.getRoster(USER_ID, 1L))
                .isInstanceOf(CustomException.class)
                .satisfies(
                        e ->
                                assertThat(((CustomException) e).getErrorCode())
                                        .isEqualTo(CommonErrorCode.FORBIDDEN));
    }
}
