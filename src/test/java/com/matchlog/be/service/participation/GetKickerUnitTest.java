package com.matchlog.be.service.participation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.matchlog.be.constant.participation.ParticipationRole;
import com.matchlog.be.domain.participation.Participation;
import com.matchlog.be.domain.player.Player;
import com.matchlog.be.domain.team.Team;
import com.matchlog.be.domain.user.User;
import com.matchlog.be.dto.participation.response.KickerResponseDto;
import com.matchlog.be.exception.CustomException;
import com.matchlog.be.exception.constant.CommonErrorCode;
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
class GetKickerUnitTest {

    private static final Long USER_ID = 1L;
    private static final Long TEAM_ID = 1L;

    @Mock private ParticipationRepository participationRepository;
    @Mock private TeamRepository teamRepository;
    @Mock private PlayerService playerService;
    @Mock private TeamAuthorizationService teamAuthorizationService;
    @InjectMocks private ParticipationService participationService;

    @Test
    void 팀_소속_멤버면_키커_정보_조회에_성공한다() {
        Player requester = Player.builder().id(9L).build();
        User targetUser = User.builder().id(2L).name("임준혁").build();
        Player target = Player.builder().id(10L).user(targetUser).build();
        Team team = Team.builder().id(TEAM_ID).build();
        Participation targetParticipation =
                Participation.create(target, team, ParticipationRole.PLAYER);
        targetParticipation.assignKickerRoles(true, false, true, false, false, false);

        when(playerService.getCurrentPlayer(USER_ID)).thenReturn(requester);
        when(participationRepository.existsByTeam_IdAndPlayer_Id(TEAM_ID, 9L)).thenReturn(true);
        when(participationRepository.findByTeam_IdAndPlayer_Id(TEAM_ID, 10L))
                .thenReturn(Optional.of(targetParticipation));

        KickerResponseDto response = participationService.getKicker(USER_ID, TEAM_ID, 10L);

        assertThat(response.getPlayerId()).isEqualTo(10L);
        assertThat(response.getName()).isEqualTo("임준혁");
        assertThat(response.isCaptain()).isTrue();
        assertThat(response.isFkRight()).isTrue();
        assertThat(response.isPkTaker()).isFalse();
    }

    @Test
    void 팀_소속이_아니면_FORBIDDEN_예외가_발생한다() {
        Player requester = Player.builder().id(9L).build();

        when(playerService.getCurrentPlayer(USER_ID)).thenReturn(requester);
        when(participationRepository.existsByTeam_IdAndPlayer_Id(TEAM_ID, 9L)).thenReturn(false);

        assertThatThrownBy(() -> participationService.getKicker(USER_ID, TEAM_ID, 10L))
                .isInstanceOf(CustomException.class)
                .satisfies(
                        e ->
                                assertThat(((CustomException) e).getErrorCode())
                                        .isEqualTo(CommonErrorCode.FORBIDDEN));
    }

    @Test
    void 대상_참가정보가_없으면_MEMBER_NOT_FOUND_예외가_발생한다() {
        Player requester = Player.builder().id(9L).build();

        when(playerService.getCurrentPlayer(USER_ID)).thenReturn(requester);
        when(participationRepository.existsByTeam_IdAndPlayer_Id(TEAM_ID, 9L)).thenReturn(true);
        when(participationRepository.findByTeam_IdAndPlayer_Id(TEAM_ID, 999L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> participationService.getKicker(USER_ID, TEAM_ID, 999L))
                .isInstanceOf(CustomException.class)
                .satisfies(
                        e ->
                                assertThat(((CustomException) e).getErrorCode())
                                        .isEqualTo(TeamErrorCode.MEMBER_NOT_FOUND));
    }
}
