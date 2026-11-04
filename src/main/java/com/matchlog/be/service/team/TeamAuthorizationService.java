package com.matchlog.be.service.team;

import com.matchlog.be.constant.participation.ParticipationRole;
import com.matchlog.be.domain.participation.Participation;
import com.matchlog.be.exception.CustomException;
import com.matchlog.be.exception.constant.CommonErrorCode;
import com.matchlog.be.exception.constant.PlayerErrorCode;
import com.matchlog.be.exception.constant.TeamErrorCode;
import com.matchlog.be.repository.ParticipationRepository;
import com.matchlog.be.repository.PlayerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/** 팀 단위 권한(MANAGER 전용 작업) 및 소속 정책 검증을 전담. Team/Participation 등 여러 서비스에서 공유. */
@Component
@RequiredArgsConstructor
public class TeamAuthorizationService {

    private static final long MAX_TEAMS_PER_PLAYER = 2;

    private final ParticipationRepository participationRepository;
    private final PlayerRepository playerRepository;

    /** playerId가 teamId의 MANAGER가 아니면 FORBIDDEN. 비소속인 경우도 동일하게 처리. */
    public void requireManager(Long teamId, Long playerId, String forbiddenMessage) {
        Participation participation =
                participationRepository.findByTeam_IdAndPlayer_Id(teamId, playerId).orElse(null);
        if (participation == null || participation.getRole() != ParticipationRole.MANAGER) {
            throw new CustomException(CommonErrorCode.FORBIDDEN, forbiddenMessage);
        }
    }

    /** playerId가 teamId 소속이 아니면 FORBIDDEN. */
    public void requireMember(Long teamId, Long playerId) {
        if (!participationRepository.existsByTeam_IdAndPlayer_Id(teamId, playerId)) {
            throw new CustomException(CommonErrorCode.FORBIDDEN, "해당 팀에 접근 권한이 없습니다.");
        }
    }

    /** playerId의 팀 내 role 반환. 비소속이면 FORBIDDEN. */
    public ParticipationRole getRole(Long teamId, Long playerId) {
        return participationRepository
                .findByTeam_IdAndPlayer_Id(teamId, playerId)
                .orElseThrow(
                        () -> new CustomException(CommonErrorCode.FORBIDDEN, "해당 팀에 접근 권한이 없습니다."))
                .getRole();
    }

    /**
     * playerId가 이미 {@value MAX_TEAMS_PER_PLAYER}개 팀에 소속돼 있으면 TEAM_LIMIT_EXCEEDED.
     *
     * <p>count 조회와 Participation 저장 사이의 TOCTOU를 막기 위해 Player row에 비관적 락을 건다. 락 해제 전에 저장까지
     * 끝나야 하므로, 반드시 호출자의 쓰기 트랜잭션 안에서 호출해야 한다(propagation MANDATORY로 강제).
     */
    @Transactional(propagation = Propagation.MANDATORY)
    public void requireTeamLimitNotExceeded(Long playerId) {
        playerRepository
                .findByIdForUpdate(playerId)
                .orElseThrow(() -> new CustomException(PlayerErrorCode.PLAYER_NOT_FOUND));

        if (participationRepository.countByPlayer_Id(playerId) >= MAX_TEAMS_PER_PLAYER) {
            throw new CustomException(TeamErrorCode.TEAM_LIMIT_EXCEEDED);
        }
    }
}
