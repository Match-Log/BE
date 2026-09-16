package com.matchlog.be.service.vote;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import com.matchlog.be.domain.match.Match;
import com.matchlog.be.domain.player.Player;
import com.matchlog.be.domain.team.Team;
import com.matchlog.be.domain.user.User;
import com.matchlog.be.domain.vote.MvpVote;
import com.matchlog.be.dto.vote.response.MvpVoteStatusResponseDto;
import com.matchlog.be.exception.CustomException;
import com.matchlog.be.exception.constant.CommonErrorCode;
import com.matchlog.be.exception.constant.MatchErrorCode;
import com.matchlog.be.repository.MatchRepository;
import com.matchlog.be.repository.MvpVoteRepository;
import com.matchlog.be.repository.ParticipationRepository;
import com.matchlog.be.repository.PlayerRepository;
import com.matchlog.be.repository.PlayerStatRepository;
import com.matchlog.be.repository.projection.MvpVoteTally;
import com.matchlog.be.service.player.PlayerService;
import com.matchlog.be.service.team.TeamAuthorizationService;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
class GetMvpVoteStatusUnitTest {

    private static final Long USER_ID = 1L;

    @Mock private MvpVoteRepository mvpVoteRepository;
    @Mock private PlayerStatRepository playerStatRepository;
    @Mock private MatchRepository matchRepository;
    @Mock private PlayerRepository playerRepository;
    @Mock private ParticipationRepository participationRepository;
    @Mock private PlayerService playerService;
    @Mock private TeamAuthorizationService teamAuthorizationService;
    @InjectMocks private MvpVoteService mvpVoteService;

    private static MvpVoteTally tally(long playerId, long count) {
        return new MvpVoteTally() {
            @Override
            public Long getVotedPlayerId() {
                return playerId;
            }

            @Override
            public Long getVoteCount() {
                return count;
            }
        };
    }

    @Test
    void 정상_조회_시_집계와_내_투표를_반환한다() {
        Team team = Team.builder().id(1L).build();
        Match match = Match.builder().id(10L).team(team).isFinished(true).build();
        Player requester = Player.builder().id(2L).build();
        Player votedPlayer =
                Player.builder().id(3L).user(User.builder().id(30L).name("임준혁").build()).build();

        when(playerService.getCurrentPlayer(USER_ID)).thenReturn(requester);
        when(matchRepository.findById(10L)).thenReturn(Optional.of(match));
        doNothing().when(teamAuthorizationService).requireMember(1L, 2L);
        when(mvpVoteRepository.tallyByMatchId(10L)).thenReturn(List.of(tally(3L, 2)));
        when(playerRepository.findAllWithUserByIdIn(List.of(3L))).thenReturn(List.of(votedPlayer));
        when(mvpVoteRepository.findByMatch_IdAndVoter_Id(10L, 2L))
                .thenReturn(Optional.of(MvpVote.create(match, requester, votedPlayer)));

        MvpVoteStatusResponseDto response = mvpVoteService.getMvpVoteStatus(USER_ID, 10L);

        assertThat(response.getMatchId()).isEqualTo(10L);
        assertThat(response.getTotalVotes()).isEqualTo(2);
        assertThat(response.getWinners()).containsExactly(3L);
        assertThat(response.getMyVote()).isEqualTo(3L);
    }

    @Test
    void 투표가_없으면_myVote는_null이고_winners는_비어있다() {
        Team team = Team.builder().id(1L).build();
        Match match = Match.builder().id(10L).team(team).isFinished(true).build();
        Player requester = Player.builder().id(2L).build();

        when(playerService.getCurrentPlayer(USER_ID)).thenReturn(requester);
        when(matchRepository.findById(10L)).thenReturn(Optional.of(match));
        doNothing().when(teamAuthorizationService).requireMember(1L, 2L);
        when(mvpVoteRepository.tallyByMatchId(10L)).thenReturn(List.of());
        when(mvpVoteRepository.findByMatch_IdAndVoter_Id(10L, 2L)).thenReturn(Optional.empty());

        MvpVoteStatusResponseDto response = mvpVoteService.getMvpVoteStatus(USER_ID, 10L);

        assertThat(response.getTotalVotes()).isEqualTo(0);
        assertThat(response.getWinners()).isEmpty();
        assertThat(response.getMyVote()).isNull();
    }

    @Test
    void 존재하지_않는_경기면_MATCH_NOT_FOUND_예외가_발생한다() {
        Player requester = Player.builder().id(2L).build();
        when(playerService.getCurrentPlayer(USER_ID)).thenReturn(requester);
        when(matchRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> mvpVoteService.getMvpVoteStatus(USER_ID, 999L))
                .isInstanceOf(CustomException.class)
                .satisfies(
                        e ->
                                assertThat(((CustomException) e).getErrorCode())
                                        .isEqualTo(MatchErrorCode.MATCH_NOT_FOUND));
    }

    @Test
    void 팀_소속이_아니면_FORBIDDEN_예외가_발생한다() {
        Team team = Team.builder().id(1L).build();
        Match match = Match.builder().id(10L).team(team).isFinished(true).build();
        Player requester = Player.builder().id(2L).build();

        when(playerService.getCurrentPlayer(USER_ID)).thenReturn(requester);
        when(matchRepository.findById(10L)).thenReturn(Optional.of(match));
        doThrow(new CustomException(CommonErrorCode.FORBIDDEN, "해당 팀에 접근 권한이 없습니다."))
                .when(teamAuthorizationService)
                .requireMember(1L, 2L);

        assertThatThrownBy(() -> mvpVoteService.getMvpVoteStatus(USER_ID, 10L))
                .isInstanceOf(CustomException.class)
                .satisfies(
                        e ->
                                assertThat(((CustomException) e).getErrorCode())
                                        .isEqualTo(CommonErrorCode.FORBIDDEN));
    }
}