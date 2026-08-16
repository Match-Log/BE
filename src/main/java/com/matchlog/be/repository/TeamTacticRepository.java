package com.matchlog.be.repository;

import com.matchlog.be.domain.tactic.TeamTactic;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TeamTacticRepository extends JpaRepository<TeamTactic, Long> {

    // [GET /api/v1/matches/{matchId}/tactics?scope=team] 팀 전술 조회 / [PUT] upsert 시 기존 전술 조회
    Optional<TeamTactic> findByMatch_Id(Long matchId);

    // [PUT /api/v1/matches/{matchId}/tactics?scope=team] 팀 전술 존재 여부 (insert vs update 분기)
    // → 제거: findByMatch_Id 가 Optional 을 반환하므로 isPresent() 로 분기 가능. existsBy 를 별도 호출하면 DB 쿼리가 두 번
    // 발생함.
    // boolean existsByMatch_Id(Long matchId);
}
