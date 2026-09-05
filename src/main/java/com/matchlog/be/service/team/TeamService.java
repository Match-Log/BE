package com.matchlog.be.service.team;

import com.matchlog.be.constant.participation.ParticipationRole;
import com.matchlog.be.domain.participation.Participation;
import com.matchlog.be.domain.player.Player;
import com.matchlog.be.domain.team.Team;
import com.matchlog.be.dto.team.request.CreateTeamRequestDto;
import com.matchlog.be.dto.team.request.UpdateTeamRequestDto;
import com.matchlog.be.dto.team.response.CreateTeamResponseDto;
import com.matchlog.be.dto.team.response.InviteCodeResponseDto;
import com.matchlog.be.dto.team.response.MyTeamResponseDto;
import com.matchlog.be.dto.team.response.TeamResponseDto;
import com.matchlog.be.dto.team.response.UpdateTeamResponseDto;
import com.matchlog.be.exception.CustomException;
import com.matchlog.be.exception.constant.CommonErrorCode;
import com.matchlog.be.exception.constant.TeamErrorCode;
import com.matchlog.be.repository.ParticipationRepository;
import com.matchlog.be.repository.TeamRepository;
import com.matchlog.be.service.player.PlayerService;
import java.security.SecureRandom;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TeamService {

    private static final String INVITE_CODE_CHARSET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int INVITE_CODE_LENGTH = 6;
    private static final int INVITE_CODE_MAX_ATTEMPTS = 10;
    private static final long MAX_TEAMS_PER_PLAYER = 2;

    private final TeamRepository teamRepository;
    private final ParticipationRepository participationRepository;
    private final PlayerService playerService;
    private final TeamAuthorizationService teamAuthorizationService;
    private final SecureRandom random = new SecureRandom();

    @Transactional
    public CreateTeamResponseDto createTeam(Long userId, CreateTeamRequestDto request) {
        Player creator = playerService.getCurrentPlayer(userId);

        if (participationRepository.countByPlayer_Id(creator.getId()) >= MAX_TEAMS_PER_PLAYER) {
            throw new CustomException(TeamErrorCode.TEAM_LIMIT_EXCEEDED);
        }

        Team team =
                teamRepository.save(
                        Team.create(
                                request.getName(),
                                request.getTeamImage(),
                                request.getRegion(),
                                request.getFoundedYear(),
                                request.getHomeGround(),
                                generateUniqueInviteCode()));

        Participation managerParticipation =
                Participation.create(creator, team, ParticipationRole.MANAGER);
        participationRepository.save(managerParticipation);

        return CreateTeamResponseDto.from(team);
    }

    public TeamResponseDto getTeam(Long userId, Long teamId) {
        Player player = playerService.getCurrentPlayer(userId);

        Team team =
                teamRepository
                        .findById(teamId)
                        .orElseThrow(() -> new CustomException(TeamErrorCode.TEAM_NOT_FOUND));

        if (!participationRepository.existsByTeam_IdAndPlayer_Id(teamId, player.getId())) {
            throw new CustomException(CommonErrorCode.FORBIDDEN, "해당 팀에 접근 권한이 없습니다.");
        }

        return TeamResponseDto.from(team);
    }

    public List<MyTeamResponseDto> getMyTeams(Long userId) {
        Player player = playerService.getCurrentPlayer(userId);

        return participationRepository.findMyTeamsByPlayerId(player.getId()).stream()
                .map(
                        participation ->
                                MyTeamResponseDto.from(
                                        participation.getTeam(), participation.getRole().name()))
                .toList();
    }

    public InviteCodeResponseDto getInviteCode(Long userId, Long teamId) {
        Player player = playerService.getCurrentPlayer(userId);

        Team team =
                teamRepository
                        .findById(teamId)
                        .orElseThrow(() -> new CustomException(TeamErrorCode.TEAM_NOT_FOUND));

        teamAuthorizationService.requireManager(
                teamId, player.getId(), "초대 코드 조회 권한이 없습니다. (MANAGER만 가능)");

        return InviteCodeResponseDto.builder().inviteCode(team.getInviteCode()).build();
    }

    @Transactional
    public UpdateTeamResponseDto updateTeam(
            Long userId, Long teamId, UpdateTeamRequestDto request) {
        Player player = playerService.getCurrentPlayer(userId);

        Team team =
                teamRepository
                        .findById(teamId)
                        .orElseThrow(() -> new CustomException(TeamErrorCode.TEAM_NOT_FOUND));

        teamAuthorizationService.requireManager(
                teamId, player.getId(), "팀 정보 수정 권한이 없습니다. (MANAGER만 가능)");

        team.changeInfo(
                request.getName(),
                request.getTeamImage(),
                request.getRegion(),
                request.getFoundedYear(),
                request.getHomeGround());

        return UpdateTeamResponseDto.from(team);
    }

    @Transactional
    public void deleteTeam(Long userId, Long teamId) {
        Player player = playerService.getCurrentPlayer(userId);

        Team team =
                teamRepository
                        .findById(teamId)
                        .orElseThrow(() -> new CustomException(TeamErrorCode.TEAM_NOT_FOUND));

        teamAuthorizationService.requireManager(
                teamId, player.getId(), "팀 삭제 권한이 없습니다. (MANAGER만 가능)");

        teamRepository.delete(team);
    }

    /** 6자리 영문대문자+숫자 초대코드 생성. 충돌 시 최대 {@value INVITE_CODE_MAX_ATTEMPTS}회 재시도. */
    private String generateUniqueInviteCode() {
        for (int attempt = 0; attempt < INVITE_CODE_MAX_ATTEMPTS; attempt++) {
            String candidate = generateInviteCodeCandidate();
            if (!teamRepository.existsByInviteCode(candidate)) {
                return candidate;
            }
        }
        throw new CustomException(CommonErrorCode.INTERNAL_SERVER_ERROR);
    }

    private String generateInviteCodeCandidate() {
        StringBuilder sb = new StringBuilder(INVITE_CODE_LENGTH);
        for (int i = 0; i < INVITE_CODE_LENGTH; i++) {
            sb.append(INVITE_CODE_CHARSET.charAt(random.nextInt(INVITE_CODE_CHARSET.length())));
        }
        return sb.toString();
    }
}
