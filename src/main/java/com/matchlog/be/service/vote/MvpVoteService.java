package com.matchlog.be.service.vote;

import com.matchlog.be.domain.match.Match;
import com.matchlog.be.domain.player.Player;
import com.matchlog.be.domain.stat.PlayerStat;
import com.matchlog.be.domain.vote.MvpVote;
import com.matchlog.be.dto.vote.request.SubmitMvpVoteRequestDto;
import com.matchlog.be.dto.vote.response.MvpVoteResultItemResponseDto;
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
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MvpVoteService {

    private final MvpVoteRepository mvpVoteRepository;
    private final PlayerStatRepository playerStatRepository;
    private final MatchRepository matchRepository;
    private final PlayerRepository playerRepository;
    private final ParticipationRepository participationRepository;
    private final PlayerService playerService;
    private final TeamAuthorizationService teamAuthorizationService;

    @Transactional
    public MvpVoteStatusResponseDto saveMvpVote(
            Long userId, Long matchId, SubmitMvpVoteRequestDto request) {
        if (request.getVotedPlayerId() == null) {
            throw new CustomException(CommonErrorCode.INVALID_REQUEST_BODY, "votedPlayerId는 필수입니다.");
        }

        Player voter = playerService.getCurrentPlayer(userId);
        Match match = getMatch(matchId);

        teamAuthorizationService.requireMember(match.getTeam().getId(), voter.getId());

        if (!match.isFinished()) {
            throw new CustomException(MatchErrorCode.MATCH_NOT_FINISHED, "종료된 경기에만 MVP 투표를 할 수 있습니다.");
        }

        Player votedPlayer =
                playerRepository
                        .findById(request.getVotedPlayerId())
                        .orElseThrow(() -> new CustomException(PlayerErrorCode.PLAYER_NOT_FOUND));

        if (!participationRepository.existsByTeam_IdAndPlayer_Id(
                match.getTeam().getId(), votedPlayer.getId())) {
            throw new CustomException(TeamErrorCode.MEMBER_NOT_FOUND);
        }

        Optional<MvpVote> existing =
                mvpVoteRepository.findByMatch_IdAndVoter_Id(matchId, voter.getId());
        if (existing.isPresent()) {
            existing.get().changeVotedPlayer(votedPlayer);
        } else {
            mvpVoteRepository.save(MvpVote.create(match, voter, votedPlayer));
        }

        recomputeMvp(match);

        return buildStatus(match, voter.getId());
    }

    @Transactional(readOnly = true)
    public MvpVoteStatusResponseDto getMvpVoteStatus(Long userId, Long matchId) {
        Player requester = playerService.getCurrentPlayer(userId);
        Match match = getMatch(matchId);

        teamAuthorizationService.requireMember(match.getTeam().getId(), requester.getId());

        return buildStatus(match, requester.getId());
    }

    /** 경기당 최다득표 전원(동점 포함) MVP. 득표 0표면 전원 false. PLAYER_STAT.isMvp는 이 재계산으로만 갱신됨. */
    private void recomputeMvp(Match match) {
        List<MvpVoteTally> tallies = mvpVoteRepository.tallyByMatchId(match.getId());
        long maxCount = tallies.stream().mapToLong(MvpVoteTally::getVoteCount).max().orElse(0L);

        Set<Long> winners = new HashSet<>();
        if (maxCount > 0) {
            for (MvpVoteTally tally : tallies) {
                if (tally.getVoteCount() == maxCount) {
                    winners.add(tally.getVotedPlayerId());
                }
            }
        }

        List<PlayerStat> stats = playerStatRepository.findStatsByMatchId(match.getId());
        Set<Long> existingPlayerIds = new HashSet<>();
        for (PlayerStat stat : stats) {
            existingPlayerIds.add(stat.getPlayer().getId());
            stat.changeMvpStatus(winners.contains(stat.getPlayer().getId()));
        }

        // 스탯이 아직 입력되지 않은 채로 MVP 표를 받은 선수 — 0점 스탯 row를 새로 만들어 MVP만 기록
        for (Long winnerId : winners) {
            if (!existingPlayerIds.contains(winnerId)) {
                Player winner =
                        playerRepository
                                .findById(winnerId)
                                .orElseThrow(() -> new CustomException(PlayerErrorCode.PLAYER_NOT_FOUND));
                PlayerStat newStat = PlayerStat.create(match, winner);
                newStat.changeMvpStatus(true);
                playerStatRepository.save(newStat);
            }
        }
    }

    private MvpVoteStatusResponseDto buildStatus(Match match, Long requesterId) {
        List<MvpVoteTally> tallies = mvpVoteRepository.tallyByMatchId(match.getId());

        long maxCount = tallies.stream().mapToLong(MvpVoteTally::getVoteCount).max().orElse(0L);
        int totalVotes = tallies.stream().mapToInt(t -> t.getVoteCount().intValue()).sum();

        List<Long> votedPlayerIds = tallies.stream().map(MvpVoteTally::getVotedPlayerId).toList();
        Map<Long, Player> playerById =
                votedPlayerIds.isEmpty()
                        ? Map.of()
                        : playerRepository.findAllWithUserByIdIn(votedPlayerIds).stream()
                                .collect(Collectors.toMap(Player::getId, p -> p));

        List<MvpVoteResultItemResponseDto> results =
                tallies.stream()
                        .sorted(Comparator.comparingLong(MvpVoteTally::getVoteCount).reversed())
                        .map(
                                t -> {
                                    Player player = playerById.get(t.getVotedPlayerId());
                                    return MvpVoteResultItemResponseDto.builder()
                                            .playerId(t.getVotedPlayerId())
                                            .name(player == null ? null : player.getUser().getName())
                                            .voteCount(t.getVoteCount().intValue())
                                            .build();
                                })
                        .toList();

        List<Long> winners =
                maxCount == 0
                        ? List.of()
                        : tallies.stream()
                                .filter(t -> t.getVoteCount() == maxCount)
                                .map(MvpVoteTally::getVotedPlayerId)
                                .toList();

        Long myVote =
                mvpVoteRepository
                        .findByMatch_IdAndVoter_Id(match.getId(), requesterId)
                        .map(v -> v.getVotedPlayer().getId())
                        .orElse(null);

        return MvpVoteStatusResponseDto.builder()
                .matchId(match.getId())
                .totalVotes(totalVotes)
                .results(results)
                .winners(winners)
                .myVote(myVote)
                .build();
    }

    private Match getMatch(Long matchId) {
        return matchRepository
                .findById(matchId)
                .orElseThrow(() -> new CustomException(MatchErrorCode.MATCH_NOT_FOUND));
    }
}