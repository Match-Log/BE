package com.matchlog.be.repository;

import com.matchlog.be.domain.stat.PlayerStat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PlayerStatRepository extends JpaRepository<PlayerStat, Long> {

    Optional<PlayerStat> findByMatch_IdAndPlayer_Id(Long matchId, Long playerId);

    boolean existsByMatch_IdAndPlayer_Id(Long matchId, Long playerId);

    @Query("SELECT ps FROM PlayerStat ps JOIN FETCH ps.player pl JOIN FETCH pl.user WHERE ps.match.id = :matchId")
    List<PlayerStat> findStatsByMatchId(@Param("matchId") Long matchId);

    List<PlayerStat> findByPlayer_Id(Long playerId);
}
