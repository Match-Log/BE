package com.matchlog.be.repository;

import com.matchlog.be.domain.team.Team;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TeamRepository extends JpaRepository<Team, Long> {

    // [POST /api/v1/teams/join] 초대코드로 팀 조회 — 없으면 Optional.empty()
    Optional<Team> findByInviteCode(String inviteCode);

    // [POST /api/v1/teams] 초대코드 생성 충돌 체크 — 충돌 시 재생성 재시도
    boolean existsByInviteCode(String inviteCode);
}
