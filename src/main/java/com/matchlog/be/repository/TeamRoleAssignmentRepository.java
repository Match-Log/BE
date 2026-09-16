package com.matchlog.be.repository;

import com.matchlog.be.domain.team.TeamRoleAssignment;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TeamRoleAssignmentRepository extends JpaRepository<TeamRoleAssignment, Long> {

    // [GET|PUT /api/v1/teams/{teamId}/role-assignment] 팀당 1행 — 조회/수정 진입점
    Optional<TeamRoleAssignment> findByTeam_Id(Long teamId);
}
