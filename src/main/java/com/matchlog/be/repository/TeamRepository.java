package com.matchlog.be.repository;

import com.matchlog.be.domain.team.Team;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TeamRepository extends JpaRepository<Team, Long> {

    // [POST /api/v1/teams/join] 초대코드로 팀 조회 — 없으면 Optional.empty()
    Optional<Team> findByInviteCode(String inviteCode);
}
