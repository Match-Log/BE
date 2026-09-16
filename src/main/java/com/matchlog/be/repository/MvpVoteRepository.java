package com.matchlog.be.repository;

import com.matchlog.be.domain.vote.MvpVote;
import com.matchlog.be.repository.projection.MvpVoteTally;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MvpVoteRepository extends JpaRepository<MvpVote, Long> {

    // [PUT /api/v1/matches/{matchId}/mvp-votes] upsert 시 기존 투표 조회 (1인 1표, insert vs update 분기)
    Optional<MvpVote> findByMatch_IdAndVoter_Id(Long matchId, Long voterId);

    // [GET /api/v1/matches/{matchId}/mvp-votes] 경기당 득표 집계 — 동점 판정을 위해 전체 후보 득표수 필요
    @Query(
            "SELECT v.votedPlayer.id AS votedPlayerId, COUNT(v) AS voteCount "
                    + "FROM MvpVote v WHERE v.match.id = :matchId GROUP BY v.votedPlayer.id")
    List<MvpVoteTally> tallyByMatchId(@Param("matchId") Long matchId);
}
