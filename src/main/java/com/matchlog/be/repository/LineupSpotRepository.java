package com.matchlog.be.repository;

import com.matchlog.be.domain.lineup.LineupSpot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface LineupSpotRepository extends JpaRepository<LineupSpot, Long> {

    boolean existsByLineup_IdAndPlayer_Id(Long lineupId, Long playerId);

    @Query("SELECT ls FROM LineupSpot ls JOIN FETCH ls.player WHERE ls.lineup.id = :lineupId")
    List<LineupSpot> findSpotsByLineupId(@Param("lineupId") Long lineupId);
}
