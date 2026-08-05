package com.matchlog.be.repository;

import com.matchlog.be.domain.vote.Vote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface VoteRepository extends JpaRepository<Vote, Long> {

    Optional<Vote> findByMatch_IdAndPlayer_Id(Long matchId, Long playerId);

    boolean existsByMatch_IdAndPlayer_Id(Long matchId, Long playerId);

    @Query("SELECT v FROM Vote v JOIN FETCH v.player pl JOIN FETCH pl.user WHERE v.match.id = :matchId")
    List<Vote> findVotesByMatchId(@Param("matchId") Long matchId);
}
