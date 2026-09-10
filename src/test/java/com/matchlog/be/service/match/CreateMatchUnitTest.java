package com.matchlog.be.service.match;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import com.matchlog.be.constant.match.HomeAway;
import com.matchlog.be.constant.match.MatchType;
import com.matchlog.be.domain.document.Document;
import com.matchlog.be.domain.match.Match;
import com.matchlog.be.domain.player.Player;
import com.matchlog.be.domain.team.Team;
import com.matchlog.be.dto.match.request.CreateMatchRequestDto;
import com.matchlog.be.dto.match.response.CreateMatchResponseDto;
import com.matchlog.be.exception.CustomException;
import com.matchlog.be.exception.constant.CommonErrorCode;
import com.matchlog.be.exception.constant.MatchErrorCode;
import com.matchlog.be.exception.constant.TeamErrorCode;
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
class CreateMatchUnitTest {

    private static final Long USER_ID = 1L;

    @Mock private MatchRepository matchRepository;
    @Mock private TeamRepository teamRepository;
    @Mock private ParticipationRepository participationRepository;
    @Mock private DocumentRepository documentRepository;
    @Mock private PlayerService playerService;
    @Mock private TeamAuthorizationService teamAuthorizationService;
    @InjectMocks private MatchService matchService;

    @Test
    void 유효한_요청이면_경기와_안내_게시글이_함께_생성된다() {
        Player player = Player.builder().id(9L).build();
        Team team = Team.builder().id(1L).name("FC 한강불사조").build();
        LocalDateTime matchDate = LocalDateTime.of(2099, 7, 14, 7, 0);
        CreateMatchRequestDto request =
                CreateMatchRequestDto.builder()
                        .opponent("서울 드래곤즈")
                        .matchDate(matchDate)
                        .location("한강공원 풋살장")
                        .homeAway(HomeAway.HOME)
                        .matchType(MatchType.SOCCER)
                        .build();

        when(playerService.getCurrentPlayer(USER_ID)).thenReturn(player);
        when(teamRepository.findById(1L)).thenReturn(Optional.of(team));
        doNothing().when(teamAuthorizationService).requireManager(eq(1L), eq(9L), anyString());
        when(matchRepository.existsByTeam_IdAndMatchDate(1L, matchDate)).thenReturn(false);
        when(matchRepository.save(any(Match.class)))
                .thenAnswer(
                        invocation -> {
                            Match saved = invocation.getArgument(0);
                            return Match.builder()
                                    .id(100L)
                                    .team(saved.getTeam())
                                    .opponent(saved.getOpponent())
                                    .matchDate(saved.getMatchDate())
                                    .location(saved.getLocation())
                                    .homeAway(saved.getHomeAway())
                                    .matchType(saved.getMatchType())
                                    .isFinished(false)
                                    .build();
                        });
        when(documentRepository.save(any(Document.class)))
                .thenAnswer(
                        invocation -> {
                            Document saved = invocation.getArgument(0);
                            return Document.builder()
                                    .id(5L)
                                    .team(saved.getTeam())
                                    .player(saved.getPlayer())
                                    .match(saved.getMatch())
                                    .title(saved.getTitle())
                                    .content(saved.getContent())
                                    .isPinned(saved.isPinned())
                                    .build();
                        });

        CreateMatchResponseDto response = matchService.createMatch(1L, USER_ID, request);

        assertThat(response.getMatchId()).isEqualTo(100L);
        assertThat(response.getTeamId()).isEqualTo(1L);
        assertThat(response.getBoardId()).isEqualTo(5L);
        assertThat(response.getBoardTitle()).isEqualTo("7/14 vs 서울 드래곤즈 참석 투표");
    }

    @Test
    void 존재하지_않는_팀이면_TEAM_NOT_FOUND_예외가_발생한다() {
        Player player = Player.builder().id(9L).build();
        CreateMatchRequestDto request =
                CreateMatchRequestDto.builder()
                        .opponent("서울 드래곤즈")
                        .matchDate(LocalDateTime.of(2099, 7, 14, 7, 0))
                        .homeAway(HomeAway.HOME)
                        .matchType(MatchType.SOCCER)
                        .build();

        when(playerService.getCurrentPlayer(USER_ID)).thenReturn(player);
        when(teamRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> matchService.createMatch(999L, USER_ID, request))
                .isInstanceOf(CustomException.class)
                .satisfies(
                        e ->
                                assertThat(((CustomException) e).getErrorCode())
                                        .isEqualTo(TeamErrorCode.TEAM_NOT_FOUND));
    }

    @Test
    void MANAGER가_아니면_FORBIDDEN_예외가_발생한다() {
        Player player = Player.builder().id(9L).build();
        Team team = Team.builder().id(1L).build();
        CreateMatchRequestDto request =
                CreateMatchRequestDto.builder()
                        .opponent("서울 드래곤즈")
                        .matchDate(LocalDateTime.of(2099, 7, 14, 7, 0))
                        .homeAway(HomeAway.HOME)
                        .matchType(MatchType.SOCCER)
                        .build();

        when(playerService.getCurrentPlayer(USER_ID)).thenReturn(player);
        when(teamRepository.findById(1L)).thenReturn(Optional.of(team));
        doThrow(new CustomException(CommonErrorCode.FORBIDDEN, "경기 생성 권한이 없습니다. (MANAGER만 가능)"))
                .when(teamAuthorizationService)
                .requireManager(1L, 9L, "경기 생성 권한이 없습니다. (MANAGER만 가능)");

        assertThatThrownBy(() -> matchService.createMatch(1L, USER_ID, request))
                .isInstanceOf(CustomException.class)
                .satisfies(
                        e ->
                                assertThat(((CustomException) e).getErrorCode())
                                        .isEqualTo(CommonErrorCode.FORBIDDEN));
    }

    @Test
    void 경기_날짜가_과거면_INVALID_MATCH_DATE_예외가_발생한다() {
        Player player = Player.builder().id(9L).build();
        Team team = Team.builder().id(1L).build();
        CreateMatchRequestDto request =
                CreateMatchRequestDto.builder()
                        .opponent("서울 드래곤즈")
                        .matchDate(LocalDateTime.of(2020, 1, 1, 0, 0))
                        .homeAway(HomeAway.HOME)
                        .matchType(MatchType.SOCCER)
                        .build();

        when(playerService.getCurrentPlayer(USER_ID)).thenReturn(player);
        when(teamRepository.findById(1L)).thenReturn(Optional.of(team));
        doNothing().when(teamAuthorizationService).requireManager(eq(1L), eq(9L), anyString());

        assertThatThrownBy(() -> matchService.createMatch(1L, USER_ID, request))
                .isInstanceOf(CustomException.class)
                .satisfies(
                        e ->
                                assertThat(((CustomException) e).getErrorCode())
                                        .isEqualTo(MatchErrorCode.INVALID_MATCH_DATE));
    }

    @Test
    void 동일_날짜에_이미_경기가_있으면_MATCH_ALREADY_EXISTS_예외가_발생한다() {
        Player player = Player.builder().id(9L).build();
        Team team = Team.builder().id(1L).build();
        LocalDateTime matchDate = LocalDateTime.of(2099, 7, 14, 7, 0);
        CreateMatchRequestDto request =
                CreateMatchRequestDto.builder()
                        .opponent("서울 드래곤즈")
                        .matchDate(matchDate)
                        .homeAway(HomeAway.HOME)
                        .matchType(MatchType.SOCCER)
                        .build();

        when(playerService.getCurrentPlayer(USER_ID)).thenReturn(player);
        when(teamRepository.findById(1L)).thenReturn(Optional.of(team));
        doNothing().when(teamAuthorizationService).requireManager(eq(1L), eq(9L), anyString());
        when(matchRepository.existsByTeam_IdAndMatchDate(1L, matchDate)).thenReturn(true);

        assertThatThrownBy(() -> matchService.createMatch(1L, USER_ID, request))
                .isInstanceOf(CustomException.class)
                .satisfies(
                        e ->
                                assertThat(((CustomException) e).getErrorCode())
                                        .isEqualTo(MatchErrorCode.MATCH_ALREADY_EXISTS));
    }
}
