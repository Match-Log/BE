package com.matchlog.be.service.lineup;

import com.matchlog.be.domain.lineup.Lineup;
import com.matchlog.be.domain.lineup.LineupSpot;
import com.matchlog.be.domain.match.Match;
import com.matchlog.be.domain.match.MatchGuest;
import com.matchlog.be.domain.player.Player;
import com.matchlog.be.dto.lineup.request.SaveLineupRequestDto;
import com.matchlog.be.dto.lineup.request.SaveLineupSpotRequestDto;
import com.matchlog.be.dto.lineup.response.LineupResponseDto;
import com.matchlog.be.dto.lineup.response.LineupSpotResponseDto;
import com.matchlog.be.dto.lineup.response.SaveLineupResponseDto;
import com.matchlog.be.exception.CustomException;
import com.matchlog.be.exception.constant.CommonErrorCode;
import com.matchlog.be.exception.constant.LineupErrorCode;
import com.matchlog.be.exception.constant.MatchErrorCode;
import com.matchlog.be.repository.LineupRepository;
import com.matchlog.be.repository.LineupSpotRepository;
import com.matchlog.be.repository.MatchGuestRepository;
import com.matchlog.be.repository.MatchRepository;
import com.matchlog.be.repository.ParticipationRepository;
import com.matchlog.be.repository.PlayerRepository;
import com.matchlog.be.service.player.PlayerService;
import com.matchlog.be.service.team.TeamAuthorizationService;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LineupService {

    private final LineupRepository lineupRepository;
    private final LineupSpotRepository lineupSpotRepository;
    private final MatchRepository matchRepository;
    private final PlayerRepository playerRepository;
    private final MatchGuestRepository matchGuestRepository;
    private final ParticipationRepository participationRepository;
    private final PlayerService playerService;
    private final TeamAuthorizationService teamAuthorizationService;

    @Transactional
    public SaveLineupResponseDto saveLineup(
            Long userId, Long matchId, SaveLineupRequestDto request) {
        Player player = playerService.getCurrentPlayer(userId);
        Match match =
                matchRepository
                        .findById(matchId)
                        .orElseThrow(() -> new CustomException(MatchErrorCode.MATCH_NOT_FOUND));

        teamAuthorizationService.requireManager(
                match.getTeam().getId(), player.getId(), "라인업은 매니저만 저장할 수 있습니다.");

        validateSpots(request.getSpots(), matchId);

        List<SaveLineupSpotRequestDto> playerSpots =
                request.getSpots().stream().filter(s -> s.getPlayerId() != null).toList();
        List<SaveLineupSpotRequestDto> guestSpots =
                request.getSpots().stream().filter(s -> s.getGuestId() != null).toList();

        Map<Long, Player> playerMap =
                playerRepository
                        .findAllById(
                                playerSpots.stream()
                                        .map(SaveLineupSpotRequestDto::getPlayerId)
                                        .toList())
                        .stream()
                        .collect(Collectors.toMap(Player::getId, p -> p));

        Map<Long, MatchGuest> guestMap =
                matchGuestRepository
                        .findAllById(
                                guestSpots.stream()
                                        .map(SaveLineupSpotRequestDto::getGuestId)
                                        .toList())
                        .stream()
                        .collect(Collectors.toMap(MatchGuest::getId, g -> g));

        Lineup lineup =
                lineupRepository
                        .findByMatch_IdAndQuarter(matchId, request.getQuarter())
                        .map(
                                existing -> {
                                    existing.changeFormation(request.getFormation());
                                    lineupSpotRepository.deleteByLineup_Id(existing.getId());
                                    return existing;
                                })
                        .orElseGet(
                                () ->
                                        lineupRepository.save(
                                                Lineup.create(
                                                        match,
                                                        request.getQuarter(),
                                                        request.getFormation())));

        List<LineupSpot> spots = new ArrayList<>();
        for (SaveLineupSpotRequestDto s : playerSpots) {
            spots.add(
                    LineupSpot.createForPlayer(
                            lineup,
                            playerMap.get(s.getPlayerId()),
                            s.getPosition(),
                            s.isStarter()));
        }
        for (SaveLineupSpotRequestDto s : guestSpots) {
            MatchGuest guest = guestMap.get(s.getGuestId());
            if (guest == null) throw new CustomException(LineupErrorCode.GUEST_NOT_FOUND);
            if (!guest.getMatch().getId().equals(matchId))
                throw new CustomException(LineupErrorCode.GUEST_NOT_IN_MATCH);
            spots.add(LineupSpot.createForGuest(lineup, guest, s.getPosition(), s.isStarter()));
        }
        lineupSpotRepository.saveAll(spots);

        return SaveLineupResponseDto.from(lineup);
    }

    @Transactional(readOnly = true)
    public List<LineupResponseDto> getLineup(Long userId, Long matchId) {
        Player player = playerService.getCurrentPlayer(userId);
        Match match =
                matchRepository
                        .findById(matchId)
                        .orElseThrow(() -> new CustomException(MatchErrorCode.MATCH_NOT_FOUND));

        if (!participationRepository.existsByTeam_IdAndPlayer_Id(
                match.getTeam().getId(), player.getId())) {
            throw new CustomException(CommonErrorCode.FORBIDDEN, "해당 팀의 팀원만 라인업을 조회할 수 있습니다.");
        }

        List<Lineup> lineups = lineupRepository.findByMatch_Id(matchId);
        List<LineupSpot> allSpots = lineupSpotRepository.findSpotsByMatchId(matchId);

        Map<Long, List<LineupSpotResponseDto>> spotsByLineupId =
                allSpots.stream()
                        .collect(
                                Collectors.groupingBy(
                                        ls -> ls.getLineup().getId(),
                                        Collectors.mapping(
                                                LineupSpotResponseDto::from, Collectors.toList())));

        return lineups.stream()
                .map(
                        l ->
                                LineupResponseDto.of(
                                        l, spotsByLineupId.getOrDefault(l.getId(), List.of())))
                .toList();
    }

    private void validateSpots(List<SaveLineupSpotRequestDto> spots, Long matchId) {
        Set<Long> playerIds = new HashSet<>();
        Set<Long> guestIds = new HashSet<>();

        for (SaveLineupSpotRequestDto spot : spots) {
            boolean hasPlayer = spot.getPlayerId() != null;
            boolean hasGuest = spot.getGuestId() != null;

            if (hasPlayer == hasGuest) {
                throw new CustomException(LineupErrorCode.INVALID_SPOT_SUBJECT);
            }
            if (hasPlayer && !playerIds.add(spot.getPlayerId())) {
                throw new CustomException(LineupErrorCode.DUPLICATE_PLAYER_IN_LINEUP);
            }
            if (hasGuest && !guestIds.add(spot.getGuestId())) {
                throw new CustomException(LineupErrorCode.DUPLICATE_GUEST_IN_LINEUP);
            }
        }
    }
}
