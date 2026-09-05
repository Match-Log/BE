package com.matchlog.be.service.participation;

import com.matchlog.be.constant.participation.ParticipationRole;
import com.matchlog.be.domain.participation.Participation;
import com.matchlog.be.domain.player.Player;
import com.matchlog.be.domain.team.Team;
import com.matchlog.be.dto.participation.request.AssignKickerRequestDto;
import com.matchlog.be.dto.participation.request.JoinTeamRequestDto;
import com.matchlog.be.dto.participation.request.UpdateParticipationRequestDto;
import com.matchlog.be.dto.participation.response.AssignKickerResponseDto;
import com.matchlog.be.dto.participation.response.JoinTeamResponseDto;
import com.matchlog.be.dto.participation.response.KickerResponseDto;
import com.matchlog.be.dto.participation.response.RosterItemResponseDto;
import com.matchlog.be.dto.participation.response.UpdateParticipationResponseDto;
import com.matchlog.be.exception.CustomException;
import com.matchlog.be.exception.constant.CommonErrorCode;
import com.matchlog.be.exception.constant.ParticipationErrorCode;
import com.matchlog.be.exception.constant.TeamErrorCode;
import com.matchlog.be.repository.ParticipationRepository;
import com.matchlog.be.repository.TeamRepository;
import com.matchlog.be.service.player.PlayerService;
import com.matchlog.be.service.team.TeamAuthorizationService;
import java.util.List;
import java.util.Optional;
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

        if (requester.getId().equals(targetPlayerId)) {
            throw new CustomException(CommonErrorCode.FORBIDDEN, "본인은 제외할 수 없습니다. 탈퇴 API를 사용하세요.");
        }

        if (target.getRole() == ParticipationRole.MANAGER
                && participationRepository.countByTeam_IdAndRole(teamId, ParticipationRole.MANAGER)
                        <= 1) {
            throw new CustomException(TeamErrorCode.LAST_MANAGER_CANNOT_BE_REMOVED);
        }

        participationRepository.delete(target);
    }

    @Transactional
    public AssignKickerResponseDto assignKicker(
            Long userId, Long teamId, Long targetPlayerId, AssignKickerRequestDto request) {
        Player requester = playerService.getCurrentPlayer(userId);

        teamAuthorizationService.requireManager(
                teamId, requester.getId(), "전담 키커 지정 권한이 없습니다. (MANAGER만 가능)");

        Participation target =
                participationRepository
                        .findByTeam_IdAndPlayer_Id(teamId, targetPlayerId)
                        .orElseThrow(() -> new CustomException(TeamErrorCode.MEMBER_NOT_FOUND));

        boolean newIsCaptain = mergeFlag(request.getIsCaptain(), target.isCaptain());
        boolean newIsPkTaker = mergeFlag(request.getIsPkTaker(), target.isPkTaker());
        boolean newIsFkRight = mergeFlag(request.getIsFkRight(), target.isFkRight());
        boolean newIsFkLeft = mergeFlag(request.getIsFkLeft(), target.isFkLeft());
        boolean newIsCkRight = mergeFlag(request.getIsCkRight(), target.isCkRight());
        boolean newIsCkLeft = mergeFlag(request.getIsCkLeft(), target.isCkLeft());

        if (newIsCaptain) {
            requireNoOtherAssignee(
                    participationRepository.findCurrentCaptainByTeamId(teamId),
                    targetPlayerId,
                    TeamErrorCode.CAPTAIN_ALREADY_ASSIGNED);
        }
        if (newIsPkTaker) {
            requireNoOtherAssignee(
                    participationRepository.findCurrentPkTakerByTeamId(teamId),
                    targetPlayerId,
                    TeamErrorCode.PK_TAKER_ALREADY_ASSIGNED);
        }

        target.assignKickerRoles(
                newIsCaptain, newIsPkTaker, newIsFkRight, newIsFkLeft, newIsCkRight, newIsCkLeft);

        return AssignKickerResponseDto.from(target);
    }

    @Transactional
    public UpdateParticipationResponseDto updateParticipation(
            Long userId, Long teamId, Long targetPlayerId, UpdateParticipationRequestDto request) {
        Player requester = playerService.getCurrentPlayer(userId);

        Participation requesterParticipation =
                participationRepository
                        .findByTeam_IdAndPlayer_Id(teamId, requester.getId())
                        .orElseThrow(
                                () ->
                                        new CustomException(
                                                CommonErrorCode.FORBIDDEN, "해당 팀에 접근 권한이 없습니다."));

        if (requesterParticipation.getRole() != ParticipationRole.MANAGER) {
            throw new CustomException(
                    CommonErrorCode.FORBIDDEN, "참가 정보 수정 권한이 없습니다. (MANAGER만 가능)");
        }

        Participation target =
                participationRepository
                        .findByTeam_IdAndPlayer_Id(teamId, targetPlayerId)
                        .orElseThrow(
                                () ->
                                        new CustomException(
                                                ParticipationErrorCode.PARTICIPATION_NOT_FOUND));

        boolean changingRole = request.getRole() != null;

        if (changingRole && requester.getId().equals(targetPlayerId)) {
            throw new CustomException(CommonErrorCode.FORBIDDEN, "본인의 역할은 변경할 수 없습니다.");
        }

        if (changingRole) {
            target.changeRole(request.getRole());
        }

        if (request.getNumber() != null
                || request.getMainPosition() != null
                || request.getSubPosition() != null) {
            target.changePosition(
                    request.getNumber(), request.getMainPosition(), request.getSubPosition());
        }

        return UpdateParticipationResponseDto.from(target);
    }

    public KickerResponseDto getKicker(Long userId, Long teamId, Long targetPlayerId) {
        Player requester = playerService.getCurrentPlayer(userId);

        if (!participationRepository.existsByTeam_IdAndPlayer_Id(teamId, requester.getId())) {
            throw new CustomException(CommonErrorCode.FORBIDDEN, "해당 팀에 접근 권한이 없습니다.");
        }

        Participation target =
                participationRepository
                        .findByTeam_IdAndPlayer_Id(teamId, targetPlayerId)
                        .orElseThrow(() -> new CustomException(TeamErrorCode.MEMBER_NOT_FOUND));

        return KickerResponseDto.from(target);
    }

    /** request 값이 있으면 그 값을, 없으면(null) 기존 값을 유지. */
    private boolean mergeFlag(Boolean requested, boolean current) {
        return requested != null ? requested : current;
    }

    /** 이미 다른 선수가 해당 역할(주장/PK키커)을 갖고 있으면 예외. 대상 본인이 이미 갖고 있던 경우(재확인)는 통과. */
    private void requireNoOtherAssignee(
            Optional<Participation> currentAssignee, Long targetPlayerId, TeamErrorCode errorCode) {
        currentAssignee.ifPresent(
                p -> {
                    if (!p.getPlayer().getId().equals(targetPlayerId)) {
                        throw new CustomException(errorCode);
                    }
                });
    }
}
