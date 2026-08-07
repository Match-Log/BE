package com.matchlog.be.repository;

import com.matchlog.be.domain.vote.Vote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface VoteRepository extends JpaRepository<Vote, Long> {

    // [PATCH /api/v1/matches/{matchId}/votes] 투표 수정 시 기존 투표 조회
    Optional<Vote> findByMatch_IdAndPlayer_Id(Long matchId, Long playerId);

    // [POST /api/v1/matches/{matchId}/votes] 중복 투표 체크
    boolean existsByMatch_IdAndPlayer_Id(Long matchId, Long playerId);

    // [GET /api/v1/matches/{matchId}/votes] 경기 투표 현황 조회 — Vote → Player → User fetch join
    // 규칙§3: 투표 현황 화면에 선수 이름·프로필 이미지 필요.
    @Query("SELECT v FROM Vote v JOIN FETCH v.player pl JOIN FETCH pl.user WHERE v.match.id = :matchId")
    List<Vote> findVotesByMatchId(@Param("matchId") Long matchId);
}
