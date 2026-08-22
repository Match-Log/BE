package com.matchlog.be.service.team;

import com.matchlog.be.constant.participation.ParticipationRole;
import com.matchlog.be.domain.participation.Participation;
import com.matchlog.be.exception.CustomException;
import com.matchlog.be.exception.constant.CommonErrorCode;
import com.matchlog.be.repository.ParticipationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** 팀 단위 권한(MANAGER 전용 작업) 검증을 전담. Team/Participation 등 여러 서비스에서 공유. */
@Component
@RequiredArgsConstructor
public class TeamAuthorizationService {

    private final ParticipationRepository participationRepository;

    /** playerId가 teamId의 MANAGER가 아니면 FORBIDDEN. 비소속인 경우도 동일하게 처리. */
    public void requireManager(Long teamId, Long playerId, String forbiddenMessage) {
        Participation participation =
                participationRepository.findByTeam_IdAndPlayer_Id(teamId, playerId).orElse(null);
        if (participation == null || participation.getRole() != ParticipationRole.MANAGER) {
            throw new CustomException(CommonErrorCode.FORBIDDEN, forbiddenMessage);
        }
    }
}
