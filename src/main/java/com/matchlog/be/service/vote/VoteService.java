package com.matchlog.be.service.vote;

import com.matchlog.be.constant.vote.VoteStatus;
import com.matchlog.be.domain.match.Match;
import com.matchlog.be.domain.player.Player;
import com.matchlog.be.domain.vote.Vote;
import com.matchlog.be.dto.vote.request.SubmitVoteRequestDto;
import com.matchlog.be.dto.vote.response.VoteItemResponseDto;
import com.matchlog.be.dto.vote.response.VoteResponseDto;
import com.matchlog.be.dto.vote.response.VoteStatusResponseDto;
import com.matchlog.be.exception.CustomException;
import com.matchlog.be.exception.constant.MatchErrorCode;
import com.matchlog.be.repository.MatchRepository;
import com.matchlog.be.repository.VoteRepository;
import com.matchlog.be.service.player.PlayerService;
import com.matchlog.be.service.team.TeamAuthorizationService;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class VoteService {

    private final VoteRepository voteRepository;
    private final MatchRepository matchRepository;
    private final PlayerService playerService;
    private final TeamAuthorizationService teamAuthorizationService;

    @Transactional
    public VoteResponseDto saveVote(Long userId, Long matchId, SubmitVoteRequestDto request) {
        Player player = playerService.getCurrentPlayer(userId);

        Match match =
                matchRepository
                        .findById(matchId)
                        .orElseThrow(() -> new CustomException(MatchErrorCode.MATCH_NOT_FOUND));

        teamAuthorizationService.requireMember(match.getTeam().getId(), player.getId());

        if (match.isFinished()) {
            throw new CustomException(MatchErrorCode.MATCH_ALREADY_FINISHED);
        }

        if (match.isVoteDeadlinePassed()) {
            throw new CustomException(MatchErrorCode.VOTE_DEADLINE_PASSED);
        }

        Optional<Vote> existing =
                voteRepository.findByMatch_IdAndPlayer_Id(matchId, player.getId());

        Vote vote;
        if (existing.isPresent()) {
            vote = existing.get();
            vote.changeStatus(request.getStatus());
        } else {
            vote = voteRepository.save(Vote.create(match, player, request.getStatus()));
        }

        return VoteResponseDto.from(vote);
    }

    @Transactional(readOnly = true)
    public VoteStatusResponseDto getVoteStatus(Long userId, Long matchId) {
        Player player = playerService.getCurrentPlayer(userId);

        Match match =
                matchRepository
                        .findById(matchId)
                        .orElseThrow(() -> new CustomException(MatchErrorCode.MATCH_NOT_FOUND));

        teamAuthorizationService.requireMember(match.getTeam().getId(), player.getId());

        List<Vote> votes = voteRepository.findVotesByMatchId(matchId);

        int attend = (int) votes.stream().filter(v -> v.getStatus() == VoteStatus.ATTEND).count();
        int pending = (int) votes.stream().filter(v -> v.getStatus() == VoteStatus.PENDING).count();
        int absent = (int) votes.stream().filter(v -> v.getStatus() == VoteStatus.ABSENT).count();

        return VoteStatusResponseDto.builder()
                .matchId(matchId)
                .total(votes.size())
                .attend(attend)
                .pending(pending)
                .absent(absent)
                .votes(votes.stream().map(VoteItemResponseDto::from).toList())
                .build();
    }
}
