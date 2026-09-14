package com.matchlog.be.service.team;

import com.matchlog.be.domain.participation.Participation;
import com.matchlog.be.domain.player.Player;
import com.matchlog.be.domain.team.TeamRoleAssignment;
import com.matchlog.be.dto.team.request.UpdateRoleAssignmentRequestDto;
import com.matchlog.be.dto.team.response.RoleAssignmentResponseDto;
import com.matchlog.be.exception.CustomException;
import com.matchlog.be.exception.constant.CommonErrorCode;
import com.matchlog.be.exception.constant.TeamErrorCode;
import com.matchlog.be.repository.ParticipationRepository;
import com.matchlog.be.repository.TeamRepository;
import com.matchlog.be.repository.TeamRoleAssignmentRepository;
import com.matchlog.be.service.player.PlayerService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TeamRoleAssignmentService {

    private final TeamRoleAssignmentRepository teamRoleAssignmentRepository;
    private final ParticipationRepository participationRepository;
    private final TeamRepository teamRepository;
    private final PlayerService playerService;
    private final TeamAuthorizationService teamAuthorizationService;

    @Transactional(readOnly = true)
    public RoleAssignmentResponseDto getRoleAssignment(Long userId, Long teamId) {
        Player player = playerService.getCurrentPlayer(userId);

        if (!teamRepository.existsById(teamId)) {
            throw new CustomException(TeamErrorCode.TEAM_NOT_FOUND);
        }

        teamAuthorizationService.requireMember(teamId, player.getId());

        return RoleAssignmentResponseDto.from(findAssignmentByTeamId(teamId));
    }

    @Transactional
    public RoleAssignmentResponseDto updateRoleAssignment(
            Long userId, Long teamId, UpdateRoleAssignmentRequestDto request) {
        Player player = playerService.getCurrentPlayer(userId);

        if (!teamRepository.existsById(teamId)) {
            throw new CustomException(TeamErrorCode.TEAM_NOT_FOUND);
        }

        teamAuthorizationService.requireManager(
                teamId, player.getId(), "세트피스 역할 지정 권한이 없습니다. (MANAGER만 가능)");

        TeamRoleAssignment assignment = findAssignmentByTeamId(teamId);

        assignment.updateRoles(
                resolveParticipation(teamId, request.getCaptainParticipationId()),
                resolveParticipation(teamId, request.getPkTakerParticipationId()),
                resolveParticipation(teamId, request.getCkLeftParticipationId()),
                resolveParticipation(teamId, request.getCkRightParticipationId()),
                resolveParticipation(teamId, request.getFkLeftParticipationId()),
                resolveParticipation(teamId, request.getFkRightParticipationId()));

        return RoleAssignmentResponseDto.from(assignment);
    }

    /** 팀 생성 시 함께 생성되는 불변식이 지켜지는 한 항상 존재. 없으면 데이터 정합성이 깨진 것이므로 500. */
    private TeamRoleAssignment findAssignmentByTeamId(Long teamId) {
        return teamRoleAssignmentRepository
                .findByTeam_Id(teamId)
                .orElseThrow(() -> new CustomException(CommonErrorCode.INTERNAL_SERVER_ERROR));
    }

    private Participation resolveParticipation(Long teamId, Long participationId) {
        if (participationId == null) {
            return null;
        }
        Participation participation =
                participationRepository
                        .findById(participationId)
                        .orElseThrow(() -> new CustomException(TeamErrorCode.MEMBER_NOT_FOUND));
        if (!participation.getTeam().getId().equals(teamId)) {
            throw new CustomException(TeamErrorCode.MEMBER_NOT_FOUND);
        }
        return participation;
    }
}
