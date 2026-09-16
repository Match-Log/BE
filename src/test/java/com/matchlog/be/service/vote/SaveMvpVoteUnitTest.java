package com.matchlog.be.service.vote;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import com.matchlog.be.domain.match.Match;
import com.matchlog.be.domain.player.Player;
import com.matchlog.be.domain.stat.PlayerStat;
import com.matchlog.be.domain.team.Team;
import com.matchlog.be.domain.user.User;
import com.matchlog.be.domain.vote.MvpVote;
import com.matchlog.be.dto.vote.request.SubmitMvpVoteRequestDto;
import com.matchlog.be.dto.vote.response.MvpVoteStatusResponseDto;
import com.matchlog.be.exception.CustomException;
import com.matchlog.be.exception.constant.CommonErrorCode;
import com.matchlog.be.exception.constant.MatchErrorCode;
import com.matchlog.be.exception.constant.PlayerErrorCode;
import com.matchlog.be.exception.constant.TeamErrorCode;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
class SaveMvpVoteUnitTest {

    private static final Long USER_ID = 1L;

    @Mock private MvpVoteRepository mvpVoteRepository;
    @Mock private PlayerStatRepository playerStatRepository;
    @Mock private MatchRepository matchRepository;
    @Mock private PlayerRepository playerRepository;
    @Mock private ParticipationRepository participationRepository;
    @Mock private PlayerService playerService;
    @Mock private TeamAuthorizationService teamAuthorizationService;
    @InjectMocks private MvpVoteService mvpVoteService;

    private Team team;
    private Match match;
    private Player voter;

    @BeforeEach
    void setUp() {
        team = Team.builder().id(1L).build();
        match = Match.builder().id(10L).team(team).isFinished(true).build();
        voter = Player.builder().id(2L).build();
    }

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
    void 첫_투표면_득표한_선수가_MVP로_기록된다() {
        Player votedPlayer =
                Player.builder().id(3L).user(User.builder().id(30L).name("임준혁").build()).build();
        SubmitMvpVoteRequestDto request = SubmitMvpVoteRequestDto.builder().votedPlayerId(3L).build();

        when(playerService.getCurrentPlayer(USER_ID)).thenReturn(voter);
        when(matchRepository.findById(10L)).thenReturn(Optional.of(match));
        doNothing().when(teamAuthorizationService).requireMember(1L, 2L);
        when(playerRepository.findById(3L)).thenReturn(Optional.of(votedPlayer));
        when(participationRepository.existsByTeam_IdAndPlayer_Id(1L, 3L)).thenReturn(true);
        when(mvpVoteRepository.findByMatch_IdAndVoter_Id(10L, 2L))
                .thenReturn(Optional.empty())
                .thenReturn(Optional.of(MvpVote.create(match, voter, votedPlayer)));
        when(mvpVoteRepository.save(any(MvpVote.class))).thenAnswer(inv -> inv.getArgument(0));
        when(mvpVoteRepository.tallyByMatchId(10L)).thenReturn(List.of(tally(3L, 1)));

        PlayerStat stat = PlayerStat.create(match, votedPlayer);
        when(playerStatRepository.findStatsByMatchId(10L)).thenReturn(List.of(stat));
        when(playerRepository.findAllWithUserByIdIn(List.of(3L))).thenReturn(List.of(votedPlayer));

        MvpVoteStatusResponseDto response = mvpVoteService.saveMvpVote(USER_ID, 10L, request);

        assertThat(stat.isMvp()).isTrue();
        assertThat(response.getTotalVotes()).isEqualTo(1);
        assertThat(response.getWinners()).containsExactly(3L);
        assertThat(response.getResults()).hasSize(1);
        assertThat(response.getResults().get(0).getName()).isEqualTo("임준혁");
        assertThat(response.getMyVote()).isEqualTo(3L);
    }

    @Test
    void 동점이면_동점자_전원이_MVP가_된다() {
        Player p3 = Player.builder().id(3L).user(User.builder().id(30L).name("A").build()).build();
        Player p4 = Player.builder().id(4L).user(User.builder().id(40L).name("B").build()).build();
        SubmitMvpVoteRequestDto request = SubmitMvpVoteRequestDto.builder().votedPlayerId(4L).build();

        when(playerService.getCurrentPlayer(USER_ID)).thenReturn(voter);
        when(matchRepository.findById(10L)).thenReturn(Optional.of(match));
        doNothing().when(teamAuthorizationService).requireMember(1L, 2L);
        when(playerRepository.findById(4L)).thenReturn(Optional.of(p4));
        when(participationRepository.existsByTeam_IdAndPlayer_Id(1L, 4L)).thenReturn(true);
        when(mvpVoteRepository.findByMatch_IdAndVoter_Id(10L, 2L)).thenReturn(Optional.empty());
        when(mvpVoteRepository.save(any(MvpVote.class))).thenAnswer(inv -> inv.getArgument(0));
        when(mvpVoteRepository.tallyByMatchId(10L)).thenReturn(List.of(tally(3L, 2), tally(4L, 2)));

        PlayerStat stat3 = PlayerStat.create(match, p3);
        PlayerStat stat4 = PlayerStat.create(match, p4);
        when(playerStatRepository.findStatsByMatchId(10L)).thenReturn(List.of(stat3, stat4));
        when(playerRepository.findAllWithUserByIdIn(anyList())).thenReturn(List.of(p3, p4));

        MvpVoteStatusResponseDto response = mvpVoteService.saveMvpVote(USER_ID, 10L, request);

        assertThat(stat3.isMvp()).isTrue();
        assertThat(stat4.isMvp()).isTrue();
        assertThat(response.getWinners()).containsExactlyInAnyOrder(3L, 4L);
    }

    @Test
    void 아직_스탯_row가_없는_선수가_득표하면_스탯_row가_새로_생성되며_MVP로_기록된다() {
        Player votedPlayer =
                Player.builder().id(5L).user(User.builder().id(50L).name("C").build()).build();
        SubmitMvpVoteRequestDto request = SubmitMvpVoteRequestDto.builder().votedPlayerId(5L).build();

        when(playerService.getCurrentPlayer(USER_ID)).thenReturn(voter);
        when(matchRepository.findById(10L)).thenReturn(Optional.of(match));
        doNothing().when(teamAuthorizationService).requireMember(1L, 2L);
        when(playerRepository.findById(5L)).thenReturn(Optional.of(votedPlayer));
        when(participationRepository.existsByTeam_IdAndPlayer_Id(1L, 5L)).thenReturn(true);
        when(mvpVoteRepository.findByMatch_IdAndVoter_Id(10L, 2L)).thenReturn(Optional.empty());
        when(mvpVoteRepository.save(any(MvpVote.class))).thenAnswer(inv -> inv.getArgument(0));
        when(mvpVoteRepository.tallyByMatchId(10L)).thenReturn(List.of(tally(5L, 1)));
        when(playerStatRepository.findStatsByMatchId(10L)).thenReturn(List.of());
        when(playerStatRepository.save(any(PlayerStat.class))).thenAnswer(inv -> inv.getArgument(0));
        when(playerRepository.findAllWithUserByIdIn(List.of(5L))).thenReturn(List.of(votedPlayer));

        mvpVoteService.saveMvpVote(USER_ID, 10L, request);

        org.mockito.ArgumentCaptor<PlayerStat> captor =
                org.mockito.ArgumentCaptor.forClass(PlayerStat.class);
        org.mockito.Mockito.verify(playerStatRepository).save(captor.capture());
        assertThat(captor.getValue().getPlayer().getId()).isEqualTo(5L);
        assertThat(captor.getValue().isMvp()).isTrue();
    }

    @Test
    void votedPlayerId가_없으면_INVALID_REQUEST_BODY_예외가_발생한다() {
        SubmitMvpVoteRequestDto request = SubmitMvpVoteRequestDto.builder().build();

        assertThatThrownBy(() -> mvpVoteService.saveMvpVote(USER_ID, 10L, request))
                .isInstanceOf(CustomException.class)
                .satisfies(
                        e ->
                                assertThat(((CustomException) e).getErrorCode())
                                        .isEqualTo(CommonErrorCode.INVALID_REQUEST_BODY));
    }

    @Test
    void 존재하지_않는_경기면_MATCH_NOT_FOUND_예외가_발생한다() {
        SubmitMvpVoteRequestDto request = SubmitMvpVoteRequestDto.builder().votedPlayerId(3L).build();

        when(playerService.getCurrentPlayer(USER_ID)).thenReturn(voter);
        when(matchRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> mvpVoteService.saveMvpVote(USER_ID, 999L, request))
                .isInstanceOf(CustomException.class)
                .satisfies(
                        e ->
                                assertThat(((CustomException) e).getErrorCode())
                                        .isEqualTo(MatchErrorCode.MATCH_NOT_FOUND));
    }

    @Test
    void 팀_소속이_아니면_FORBIDDEN_예외가_발생한다() {
        SubmitMvpVoteRequestDto request = SubmitMvpVoteRequestDto.builder().votedPlayerId(3L).build();

        when(playerService.getCurrentPlayer(USER_ID)).thenReturn(voter);
        when(matchRepository.findById(10L)).thenReturn(Optional.of(match));
        doThrow(new CustomException(CommonErrorCode.FORBIDDEN, "해당 팀에 접근 권한이 없습니다."))
                .when(teamAuthorizationService)
                .requireMember(1L, 2L);

        assertThatThrownBy(() -> mvpVoteService.saveMvpVote(USER_ID, 10L, request))
                .isInstanceOf(CustomException.class)
                .satisfies(
                        e ->
                                assertThat(((CustomException) e).getErrorCode())
                                        .isEqualTo(CommonErrorCode.FORBIDDEN));
    }

    @Test
    void 종료되지_않은_경기면_MATCH_NOT_FINISHED_예외가_발생한다() {
        Match ongoingMatch = Match.builder().id(10L).team(team).isFinished(false).build();
        SubmitMvpVoteRequestDto request = SubmitMvpVoteRequestDto.builder().votedPlayerId(3L).build();

        when(playerService.getCurrentPlayer(USER_ID)).thenReturn(voter);
        when(matchRepository.findById(10L)).thenReturn(Optional.of(ongoingMatch));
        doNothing().when(teamAuthorizationService).requireMember(1L, 2L);

        assertThatThrownBy(() -> mvpVoteService.saveMvpVote(USER_ID, 10L, request))
                .isInstanceOf(CustomException.class)
                .satisfies(
                        e ->
                                assertThat(((CustomException) e).getErrorCode())
                                        .isEqualTo(MatchErrorCode.MATCH_NOT_FINISHED));
    }

    @Test
    void 존재하지_않는_선수에게_투표하면_PLAYER_NOT_FOUND_예외가_발생한다() {
        SubmitMvpVoteRequestDto request =
                SubmitMvpVoteRequestDto.builder().votedPlayerId(999L).build();

        when(playerService.getCurrentPlayer(USER_ID)).thenReturn(voter);
        when(matchRepository.findById(10L)).thenReturn(Optional.of(match));
        doNothing().when(teamAuthorizationService).requireMember(1L, 2L);
        when(playerRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> mvpVoteService.saveMvpVote(USER_ID, 10L, request))
                .isInstanceOf(CustomException.class)
                .satisfies(
                        e ->
                                assertThat(((CustomException) e).getErrorCode())
                                        .isEqualTo(PlayerErrorCode.PLAYER_NOT_FOUND));
    }

    @Test
    void 팀_소속이_아닌_선수에게_투표하면_MEMBER_NOT_FOUND_예외가_발생한다() {
        Player votedPlayer = Player.builder().id(3L).build();
        SubmitMvpVoteRequestDto request = SubmitMvpVoteRequestDto.builder().votedPlayerId(3L).build();

        when(playerService.getCurrentPlayer(USER_ID)).thenReturn(voter);
        when(matchRepository.findById(10L)).thenReturn(Optional.of(match));
        doNothing().when(teamAuthorizationService).requireMember(1L, 2L);
        when(playerRepository.findById(3L)).thenReturn(Optional.of(votedPlayer));
        when(participationRepository.existsByTeam_IdAndPlayer_Id(eq(1L), eq(3L))).thenReturn(false);

        assertThatThrownBy(() -> mvpVoteService.saveMvpVote(USER_ID, 10L, request))
                .isInstanceOf(CustomException.class)
                .satisfies(
                        e ->
                                assertThat(((CustomException) e).getErrorCode())
                                        .isEqualTo(TeamErrorCode.MEMBER_NOT_FOUND));
    }
}
