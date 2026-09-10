package com.matchlog.be.service.match;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.matchlog.be.constant.match.HomeAway;
import com.matchlog.be.constant.match.MatchType;
import com.matchlog.be.domain.match.Match;
import com.matchlog.be.domain.player.Player;
import com.matchlog.be.domain.team.Team;
import com.matchlog.be.dto.match.response.MatchResponseDto;
import com.matchlog.be.exception.CustomException;
import com.matchlog.be.exception.constant.CommonErrorCode;
import com.matchlog.be.exception.constant.MatchErrorCode;
import com.matchlog.be.repository.DocumentRepository;
import com.matchlog.be.repository.MatchRepository;
import com.matchlog.be.repository.ParticipationRepository;
import com.matchlog.be.repository.TeamRepository;
import com.matchlog.be.service.player.PlayerService;
import com.matchlog.be.service.team.TeamAuthorizationService;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
class GetMatchUnitTest {

    private static final Long USER_ID = 1L;

    @Mock private MatchRepository matchRepository;
    @Mock private TeamRepository teamRepository;
    @Mock private ParticipationRepository participationRepository;
    @Mock private DocumentRepository documentRepository;
    @Mock private PlayerService playerService;
    @Mock private TeamAuthorizationService teamAuthorizationService;
    @InjectMocks private MatchService matchService;

    @Test
    void 팀_소속이면_경기_상세_정보를_반환한다() {
        Player player = Player.builder().id(9L).build();
        Team team = Team.builder().id(1L).build();
        Match match =
                Match.builder()
                        .id(100L)
                        .team(team)
                        .opponent("서울 드래곤즈")
                        .matchDate(LocalDateTime.of(2099, 7, 14, 7, 0))
                        .homeAway(HomeAway.HOME)
                        .matchType(MatchType.SOCCER)
                        .build();

        when(matchRepository.findById(100L)).thenReturn(Optional.of(match));
        when(playerService.getCurrentPlayer(USER_ID)).thenReturn(player);
        when(participationRepository.existsByTeam_IdAndPlayer_Id(1L, 9L)).thenReturn(true);

        MatchResponseDto response = matchService.getMatch(100L, USER_ID);

        assertThat(response.getMatchId()).isEqualTo(100L);
        assertThat(response.getOpponent()).isEqualTo("서울 드래곤즈");
    }

    @Test
    void 존재하지_않는_경기면_MATCH_NOT_FOUND_예외가_발생한다() {
        Player player = Player.builder().id(9L).build();

        when(playerService.getCurrentPlayer(USER_ID)).thenReturn(player);
        when(matchRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> matchService.getMatch(999L, USER_ID))
                .isInstanceOf(CustomException.class)
                .satisfies(
                        e ->
                                assertThat(((CustomException) e).getErrorCode())
                                        .isEqualTo(MatchErrorCode.MATCH_NOT_FOUND));
    }

    @Test
    void 경기의_팀_소속이_아니면_FORBIDDEN_예외가_발생한다() {
        Player player = Player.builder().id(9L).build();
        Team team = Team.builder().id(1L).build();
        Match match = Match.builder().id(100L).team(team).opponent("서울 드래곤즈").build();

        when(playerService.getCurrentPlayer(USER_ID)).thenReturn(player);
        when(matchRepository.findById(100L)).thenReturn(Optional.of(match));
        when(participationRepository.existsByTeam_IdAndPlayer_Id(1L, 9L)).thenReturn(false);

        assertThatThrownBy(() -> matchService.getMatch(100L, USER_ID))
                .isInstanceOf(CustomException.class)
                .satisfies(
                        e ->
                                assertThat(((CustomException) e).getErrorCode())
                                        .isEqualTo(CommonErrorCode.FORBIDDEN));
    }
}
