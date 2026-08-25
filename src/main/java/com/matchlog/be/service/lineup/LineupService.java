package com.matchlog.be.service.lineup;

import com.matchlog.be.domain.lineup.Lineup;
import com.matchlog.be.domain.lineup.LineupSpot;
import com.matchlog.be.domain.match.Match;
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
import com.matchlog.be.repository.MatchRepository;
import com.matchlog.be.repository.ParticipationRepository;
import com.matchlog.be.repository.PlayerRepository;
import com.matchlog.be.service.player.PlayerService;
import com.matchlog.be.service.team.TeamAuthorizationService;
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
    private final ParticipationRepository participationRepository;
    private final PlayerService playerService;
    private final TeamAuthorizationService teamAuthorizationService;

    @Transactional
    public SaveLineupResponseDto saveLineup(Long userId, Long matchId, SaveLineupRequestDto request) {
        Player player = playerService.getCurrentPlayer(userId);
        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new CustomException(MatchErrorCode.MATCH_NOT_FOUND));

        teamAuthorizationService.requireManager(
                match.getTeam().getId(), player.getId(), "라인업은 매니저만 저장할 수 있습니다.");

        validateNoDuplicateSpots(request.getSpots());

        Map<Long, Player> playerMap = playerRepository.findAllById(
                        request.getSpots().stream().map(SaveLineupSpotRequestDto::getPlayerId).toList())
                .stream()
                .collect(Collectors.toMap(Player::getId, p -> p));

        Lineup lineup = lineupRepository.findByMatch_IdAndQuarter(matchId, request.getQuarter())
                .map(existing -> {
                    existing.changeFormation(request.getFormation());
                    lineupSpotRepository.deleteByLineup_Id(existing.getId());
                    return existing;
                })
                .orElseGet(() -> lineupRepository.save(
                        Lineup.create(match, request.getQuarter(), request.getFormation())));

        List<LineupSpot> spots = request.getSpots().stream()
                .map(s -> LineupSpot.create(lineup, playerMap.get(s.getPlayerId()), s.getPosition(), s.isStarter()))
                .toList();
        lineupSpotRepository.saveAll(spots);

        return SaveLineupResponseDto.from(lineup);
    }

    @Transactional(readOnly = true)
    public List<LineupResponseDto> getLineup(Long userId, Long matchId) {
        Player player = playerService.getCurrentPlayer(userId);
        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new CustomException(MatchErrorCode.MATCH_NOT_FOUND));

        if (!participationRepository.existsByTeam_IdAndPlayer_Id(match.getTeam().getId(), player.getId())) {
            throw new CustomException(CommonErrorCode.FORBIDDEN, "해당 팀의 팀원만 라인업을 조회할 수 있습니다.");
        }

        List<Lineup> lineups = lineupRepository.findByMatch_Id(matchId);
        List<LineupSpot> allSpots = lineupSpotRepository.findSpotsByMatchId(matchId);

        Map<Long, List<LineupSpotResponseDto>> spotsByLineupId = allSpots.stream()
                .collect(Collectors.groupingBy(
                        ls -> ls.getLineup().getId(),
                        Collectors.mapping(LineupSpotResponseDto::from, Collectors.toList())));

        return lineups.stream()
                .map(l -> LineupResponseDto.of(l, spotsByLineupId.getOrDefault(l.getId(), List.of())))
                .toList();
    }

    private void validateNoDuplicateSpots(List<SaveLineupSpotRequestDto> spots) {
        Set<Long> playerIds = new HashSet<>();
        Set<String> positions = new HashSet<>();
        for (SaveLineupSpotRequestDto spot : spots) {
            if (!playerIds.add(spot.getPlayerId())) {
                throw new CustomException(LineupErrorCode.DUPLICATE_PLAYER_IN_LINEUP);
            }
            if (!positions.add(spot.getPosition())) {
                throw new CustomException(LineupErrorCode.DUPLICATE_POSITION_IN_LINEUP);
            }
        }
    }
}
