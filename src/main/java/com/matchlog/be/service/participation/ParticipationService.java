package com.matchlog.be.service.participation;

import com.matchlog.be.constant.participation.ParticipationRole;
import com.matchlog.be.domain.participation.Participation;
import com.matchlog.be.domain.player.Player;
import com.matchlog.be.domain.team.Team;
import com.matchlog.be.dto.participation.request.JoinTeamRequestDto;
import com.matchlog.be.dto.participation.response.JoinTeamResponseDto;
import com.matchlog.be.exception.CustomException;
import com.matchlog.be.exception.constant.TeamErrorCode;
import com.matchlog.be.repository.ParticipationRepository;
import com.matchlog.be.repository.TeamRepository;
import com.matchlog.be.service.player.PlayerService;
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
}
