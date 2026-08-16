package com.matchlog.be.repository;

import com.matchlog.be.domain.lineup.Lineup;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LineupRepository extends JpaRepository<Lineup, Long> {

    // [PUT /api/v1/matches/{matchId}/lineup] 쿼터 라인업 upsert 시 기존 라인업 조회
    Optional<Lineup> findByMatch_IdAndQuarter(Long matchId, int quarter);

    // [PUT /api/v1/matches/{matchId}/lineup] 쿼터 라인업 존재 여부 (insert vs update 분기)
    // → 제거: findByMatch_IdAndQuarter 가 Optional 을 반환하므로 isPresent() 로 분기 가능. existsBy 를 별도 호출하면 DB
    // 쿼리가 두 번 발생함.
    // boolean existsByMatch_IdAndQuarter(Long matchId, int quarter);

    // [GET /api/v1/matches/{matchId}/lineup] 경기 전 쿼터 라인업 헤더 목록 조회
    List<Lineup> findByMatch_Id(Long matchId);
}
