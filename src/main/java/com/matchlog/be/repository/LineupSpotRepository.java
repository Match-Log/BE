package com.matchlog.be.repository;

import com.matchlog.be.domain.lineup.LineupSpot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface LineupSpotRepository extends JpaRepository<LineupSpot, Long> {

    // [PUT /api/v1/matches/{matchId}/lineup] 같은 선수 라인업 중복 배치 체크
    boolean existsByLineup_IdAndPlayer_Id(Long lineupId, Long playerId);

    // [PUT /api/v1/matches/{matchId}/lineup] 쿼터 단건 스팟 조회
    // 규칙§3: LineupSpot → Player fetch join.
    @Query("SELECT ls FROM LineupSpot ls JOIN FETCH ls.player WHERE ls.lineup.id = :lineupId")
    List<LineupSpot> findSpotsByLineupId(@Param("lineupId") Long lineupId);

    // [GET /api/v1/matches/{matchId}/lineup] 경기 전 쿼터 스팟 한 번에 조회
    // 기존 패턴: findByMatch_Id(LineupRepo) + findSpotsByLineupId(×N쿼터) = N+1 발생.
    // 이 메서드 1번으로 전 쿼터 스팟 + Player를 한 쿼리에 처리. 규칙§3 N+1 방지.
    @Query("SELECT ls FROM LineupSpot ls JOIN FETCH ls.player WHERE ls.lineup.match.id = :matchId")
    List<LineupSpot> findSpotsByMatchId(@Param("matchId") Long matchId);
}
