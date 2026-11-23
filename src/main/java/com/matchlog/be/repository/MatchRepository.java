package com.matchlog.be.repository;

import com.matchlog.be.domain.match.Match;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MatchRepository extends JpaRepository<Match, Long> {

    // [POST /api/v1/teams/{teamId}/matches] 같은 날짜·팀 경기 중복 등록 체크
    boolean existsByTeam_IdAndMatchDate(Long teamId, LocalDateTime matchDate);

    // [GET /api/v1/teams/{teamId}/matches] 전체 경기 목록 — 최신 경기 우선
    List<Match> findByTeam_IdOrderByMatchDateDesc(Long teamId);

    // [GET /api/v1/teams/{teamId}/matches?status=upcoming|finished] 상태별 경기 목록
    List<Match> findByTeam_IdAndIsFinishedOrderByMatchDateDesc(Long teamId, boolean isFinished);

    // [스케줄러] 오늘 경기 알림 — 날짜 범위 내 경기 조회 (Team fetch join으로 N+1 방지)
    @Query("SELECT m FROM Match m JOIN FETCH m.team WHERE m.matchDate BETWEEN :start AND :end")
    List<Match> findMatchesWithTeamBetween(
            @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    // [스케줄러] 경기 2시간 후 피드백 알림 — 기준 시각 범위 내 경기 조회 (Team fetch join으로 N+1 방지)
    @Query(
            "SELECT m FROM Match m JOIN FETCH m.team"
                    + " WHERE m.matchDate BETWEEN :lookback AND :threshold")
    List<Match> findMatchesWithTeamForReview(
            @Param("lookback") LocalDateTime lookback, @Param("threshold") LocalDateTime threshold);
}
