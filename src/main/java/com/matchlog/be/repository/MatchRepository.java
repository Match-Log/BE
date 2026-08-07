package com.matchlog.be.repository;

import com.matchlog.be.domain.match.Match;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface MatchRepository extends JpaRepository<Match, Long> {

    // [POST /api/v1/teams/{teamId}/matches] 같은 날짜·팀 경기 중복 등록 체크
    boolean existsByTeam_IdAndMatchDate(Long teamId, LocalDateTime matchDate);

    // [GET /api/v1/teams/{teamId}/matches] 전체 경기 목록 — 최신 경기 우선
    List<Match> findByTeam_IdOrderByMatchDateDesc(Long teamId);

    // [GET /api/v1/teams/{teamId}/matches?status=upcoming|finished] 상태별 경기 목록
    List<Match> findByTeam_IdAndIsFinishedOrderByMatchDateDesc(Long teamId, boolean isFinished);
}
