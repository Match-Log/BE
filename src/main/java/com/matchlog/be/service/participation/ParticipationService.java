package com.matchlog.be.service.participation;

import com.matchlog.be.constant.participation.ParticipationRole;
import com.matchlog.be.domain.participation.Participation;
import com.matchlog.be.domain.player.Player;
import com.matchlog.be.domain.team.Team;
import com.matchlog.be.dto.participation.request.JoinTeamRequestDto;
import com.matchlog.be.dto.participation.response.JoinTeamResponseDto;
import com.matchlog.be.dto.participation.response.RosterItemResponseDto;
import com.matchlog.be.exception.CustomException;
import com.matchlog.be.exception.constant.CommonErrorCode;
import com.matchlog.be.exception.constant.ParticipationErrorCode;
import com.matchlog.be.exception.constant.TeamErrorCode;
import com.matchlog.be.repository.ParticipationRepository;
import com.matchlog.be.repository.TeamRepository;
import com.matchlog.be.service.player.PlayerService;
import com.matchlog.be.service.team.TeamAuthorizationService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ParticipationService {

    private static final long MAX_TEAMS_PER_PLAYER = 2;

    private final ParticipationRepository participationRepository;
    private final TeamRepository teamRepository;
    private final PlayerService playerService;
    private final TeamAuthorizationService teamAuthorizationService;

    @Transactional
    public JoinTeamResponseDto joinTeamByInviteCode(Long userId, JoinTeamRequestDto request) {
        Player player = playerService.getCurrentPlayer(userId);

        Team team =
                teamRepository
                        .findByInviteCode(request.getInviteCode())
                        .orElseThrow(
                                () -> new CustomException(TeamErrorCode.INVITE_CODE_NOT_FOUND));

        if (participationRepository.existsByTeam_IdAndPlayer_Id(team.getId(), player.getId())) {
            throw new CustomException(TeamErrorCode.ALREADY_JOINED);
        }

        if (participationRepository.countByPlayer_Id(player.getId()) >= MAX_TEAMS_PER_PLAYER) {
            throw new CustomException(TeamErrorCode.TEAM_LIMIT_EXCEEDED);
        }

        Participation participation =
                participationRepository.save(
                        Participation.create(player, team, ParticipationRole.PLAYER));

        return JoinTeamResponseDto.from(participation);
    }

    public List<RosterItemResponseDto> getRoster(Long userId, Long teamId) {
        Player player = playerService.getCurrentPlayer(userId);

        if (!teamRepository.existsById(teamId)) {
            throw new CustomException(TeamErrorCode.TEAM_NOT_FOUND);
        }

        if (!participationRepository.existsByTeam_IdAndPlayer_Id(teamId, player.getId())) {
            throw new CustomException(CommonErrorCode.FORBIDDEN, "해당 팀에 접근 권한이 없습니다.");
        }

        return participationRepository.findRosterByTeamId(teamId).stream()
                .map(RosterItemResponseDto::from)
                .toList();
    }

    @Transactional
    public void removeFromRoster(Long userId, Long teamId, Long targetPlayerId) {
        Player requester = playerService.getCurrentPlayer(userId);

        teamAuthorizationService.requireManager(
                teamId, requester.getId(), "팀원 제외 권한이 없습니다. (MANAGER만 가능)");

        Participation target =
                participationRepository
                        .findByTeam_IdAndPlayer_Id(teamId, targetPlayerId)
                        .orElseThrow(
                                () ->
                                        new CustomException(
                                                ParticipationErrorCode.PARTICIPATION_NOT_FOUND));

        if (target.getRole() == ParticipationRole.MANAGER
                && participationRepository.countByTeam_IdAndRole(teamId, ParticipationRole.MANAGER)
                        <= 1) {
            throw new CustomException(TeamErrorCode.LAST_MANAGER_CANNOT_BE_REMOVED);
        }

        participationRepository.delete(target);
    }
}
