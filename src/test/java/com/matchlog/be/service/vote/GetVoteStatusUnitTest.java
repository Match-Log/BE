package com.matchlog.be.service.vote;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import com.matchlog.be.constant.participation.ParticipationRole;
import com.matchlog.be.constant.vote.VoteStatus;
import com.matchlog.be.domain.match.Match;
import com.matchlog.be.domain.participation.Participation;
import com.matchlog.be.domain.player.Player;
import com.matchlog.be.domain.team.Team;
import com.matchlog.be.domain.user.User;
import com.matchlog.be.domain.vote.Vote;
import com.matchlog.be.dto.vote.response.VoteStatusResponseDto;
import com.matchlog.be.exception.CustomException;
import com.matchlog.be.exception.constant.CommonErrorCode;
import com.matchlog.be.exception.constant.MatchErrorCode;
import com.matchlog.be.repository.MatchRepository;
import com.matchlog.be.repository.ParticipationRepository;
import com.matchlog.be.repository.VoteRepository;
import com.matchlog.be.service.player.PlayerService;
import com.matchlog.be.service.team.TeamAuthorizationService;
import java.time.LocalDateTime;
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
class GetVoteStatusUnitTest {

    private static final Long USER_ID = 1L;

    @Mock private VoteRepository voteRepository;
    @Mock private MatchRepository matchRepository;
    @Mock private ParticipationRepository participationRepository;
    @Mock private PlayerService playerService;
    @Mock private TeamAuthorizationService teamAuthorizationService;
    @InjectMocks private VoteService voteService;

    private static User user(long id, String name) {
        return User.builder().id(id).name(name).profileImage(null).build();
    }

    private static Player player(long id, User user) {
        return Player.builder().id(id).user(user).build();
    }

    private static Participation participation(Player player, Team team) {
        return Participation.builder()
                .player(player)
                .team(team)
                .role(ParticipationRole.PLAYER)
                .joinedAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    private static Vote vote(Match match, Player player, VoteStatus status) {
        return Vote.builder()
                .id(null)
                .match(match)
                .player(player)
                .status(status)
                .votedAt(LocalDateTime.now())
                .build();
    }

    @Test
    void 미투표자는_PENDING으로_포함되고_total은_로스터_크기다() {
        Team team = Team.builder().id(1L).build();
        Match match = Match.builder().id(10L).team(team).build();

        Player requester = player(2L, user(20L, "요청자"));
        Player voter = player(3L, user(30L, "투표자"));
        Player nonVoter = player(4L, user(40L, "미투표자"));

        Vote existingVote = vote(match, voter, VoteStatus.ATTEND);

        when(playerService.getCurrentPlayer(USER_ID)).thenReturn(requester);
        when(matchRepository.findById(10L)).thenReturn(Optional.of(match));
        doNothing().when(teamAuthorizationService).requireMember(1L, 2L);
        when(participationRepository.findRosterByTeamId(1L))
                .thenReturn(
                        List.of(
                                participation(requester, team),
                                participation(voter, team),
                                participation(nonVoter, team)));
        when(voteRepository.findVotesByMatchId(10L)).thenReturn(List.of(existingVote));

        VoteStatusResponseDto response = voteService.getVoteStatus(USER_ID, 10L, null);

        assertThat(response.getTotal()).isEqualTo(3);
        assertThat(response.getAttend()).isEqualTo(1);
        assertThat(response.getPending()).isEqualTo(2);
        assertThat(response.getAbsent()).isEqualTo(0);
        assertThat(response.getVotes()).hasSize(3);
        var nonVoterItem =
                response.getVotes().stream()
                        .filter(v -> v.getPlayerId().equals(4L))
                        .findFirst()
                        .orElseThrow();
        assertThat(nonVoterItem.getStatus()).isEqualTo(VoteStatus.PENDING);
        assertThat(nonVoterItem.getVotedAt()).isNull();
    }

    @Test
    void 아무도_투표하지_않으면_로스터_전원이_PENDING이다() {
        Team team = Team.builder().id(1L).build();
        Match match = Match.builder().id(10L).team(team).build();

        Player requester = player(2L, user(20L, "A"));
        Player member = player(3L, user(30L, "B"));

        when(playerService.getCurrentPlayer(USER_ID)).thenReturn(requester);
        when(matchRepository.findById(10L)).thenReturn(Optional.of(match));
        doNothing().when(teamAuthorizationService).requireMember(1L, 2L);
        when(participationRepository.findRosterByTeamId(1L))
                .thenReturn(List.of(participation(requester, team), participation(member, team)));
        when(voteRepository.findVotesByMatchId(10L)).thenReturn(List.of());

        VoteStatusResponseDto response = voteService.getVoteStatus(USER_ID, 10L, null);

        assertThat(response.getTotal()).isEqualTo(2);
        assertThat(response.getPending()).isEqualTo(2);
        assertThat(response.getAttend()).isEqualTo(0);
        assertThat(response.getAbsent()).isEqualTo(0);
        assertThat(response.getVotes()).allMatch(v -> v.getStatus() == VoteStatus.PENDING);
    }

    @Test
    void status_필터_적용_시_해당_상태_항목만_반환된다() {
        Team team = Team.builder().id(1L).build();
        Match match = Match.builder().id(10L).team(team).build();

        Player requester = player(2L, user(20L, "A"));
        Player attendPlayer = player(3L, user(30L, "B"));
        Player absentPlayer = player(4L, user(40L, "C"));

        when(playerService.getCurrentPlayer(USER_ID)).thenReturn(requester);
        when(matchRepository.findById(10L)).thenReturn(Optional.of(match));
        doNothing().when(teamAuthorizationService).requireMember(1L, 2L);
        when(participationRepository.findRosterByTeamId(1L))
                .thenReturn(
                        List.of(
                                participation(requester, team),
                                participation(attendPlayer, team),
                                participation(absentPlayer, team)));
        when(voteRepository.findVotesByMatchId(10L))
                .thenReturn(
                        List.of(
                                vote(match, attendPlayer, VoteStatus.ATTEND),
                                vote(match, absentPlayer, VoteStatus.ABSENT)));

        VoteStatusResponseDto response = voteService.getVoteStatus(USER_ID, 10L, VoteStatus.ATTEND);

        assertThat(response.getTotal()).isEqualTo(3);
        assertThat(response.getVotes()).hasSize(1);
        assertThat(response.getVotes().get(0).getPlayerId()).isEqualTo(3L);
        assertThat(response.getVotes().get(0).getStatus()).isEqualTo(VoteStatus.ATTEND);
    }

    @Test
    void 존재하지_않는_경기면_MATCH_NOT_FOUND_예외가_발생한다() {
        Player requester = player(2L, user(20L, "A"));
        when(playerService.getCurrentPlayer(USER_ID)).thenReturn(requester);
        when(matchRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> voteService.getVoteStatus(USER_ID, 999L, null))
                .isInstanceOf(CustomException.class)
                .satisfies(
                        e ->
                                assertThat(((CustomException) e).getErrorCode())
                                        .isEqualTo(MatchErrorCode.MATCH_NOT_FOUND));
    }

    @Test
    void 팀_소속이_아니면_FORBIDDEN_예외가_발생한다() {
        Team team = Team.builder().id(1L).build();
        Match match = Match.builder().id(10L).team(team).build();
        Player requester = player(2L, user(20L, "A"));

        when(playerService.getCurrentPlayer(USER_ID)).thenReturn(requester);
        when(matchRepository.findById(10L)).thenReturn(Optional.of(match));
        doThrow(new CustomException(CommonErrorCode.FORBIDDEN, "해당 팀에 접근 권한이 없습니다."))
                .when(teamAuthorizationService)
                .requireMember(1L, 2L);

        assertThatThrownBy(() -> voteService.getVoteStatus(USER_ID, 10L, null))
                .isInstanceOf(CustomException.class)
                .satisfies(
                        e ->
                                assertThat(((CustomException) e).getErrorCode())
                                        .isEqualTo(CommonErrorCode.FORBIDDEN));
    }
}
