package com.matchlog.be.service.lineup;

import com.matchlog.be.domain.match.Match;
import com.matchlog.be.domain.match.MatchGuest;
import com.matchlog.be.domain.player.Player;
import com.matchlog.be.dto.lineup.request.CreateMatchGuestRequestDto;
import com.matchlog.be.dto.lineup.response.MatchGuestResponseDto;
import com.matchlog.be.exception.CustomException;
import com.matchlog.be.exception.constant.LineupErrorCode;
import com.matchlog.be.exception.constant.MatchErrorCode;
import com.matchlog.be.repository.MatchGuestRepository;
import com.matchlog.be.repository.MatchRepository;
import com.matchlog.be.service.player.PlayerService;
import com.matchlog.be.service.team.TeamAuthorizationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MatchGuestService {

    private final MatchGuestRepository matchGuestRepository;
    private final MatchRepository matchRepository;
    private final PlayerService playerService;
    private final TeamAuthorizationService teamAuthorizationService;

    @Transactional
    public MatchGuestResponseDto createGuest(
            Long userId, Long matchId, CreateMatchGuestRequestDto request) {
        Player player = playerService.getCurrentPlayer(userId);
        Match match =
                matchRepository
                        .findById(matchId)
                        .orElseThrow(() -> new CustomException(MatchErrorCode.MATCH_NOT_FOUND));

        teamAuthorizationService.requireManager(
                match.getTeam().getId(), player.getId(), "용병 등록은 매니저만 할 수 있습니다.");

        MatchGuest guest = MatchGuest.create(match, request.getName(), request.getPosition());
        return MatchGuestResponseDto.from(matchGuestRepository.save(guest));
    }

    @Transactional
    public void deleteGuest(Long userId, Long matchId, Long guestId) {
        Player player = playerService.getCurrentPlayer(userId);
        Match match =
                matchRepository
                        .findById(matchId)
                        .orElseThrow(() -> new CustomException(MatchErrorCode.MATCH_NOT_FOUND));

        teamAuthorizationService.requireManager(
                match.getTeam().getId(), player.getId(), "용병 삭제는 매니저만 할 수 있습니다.");

        MatchGuest guest =
                matchGuestRepository
                        .findById(guestId)
                        .orElseThrow(() -> new CustomException(LineupErrorCode.GUEST_NOT_FOUND));

        if (!guest.getMatch().getId().equals(matchId)) {
            throw new CustomException(LineupErrorCode.GUEST_NOT_IN_MATCH);
        }

        matchGuestRepository.delete(guest);
    }
}
