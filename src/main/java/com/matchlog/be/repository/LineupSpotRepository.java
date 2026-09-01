package com.matchlog.be.repository;

import com.matchlog.be.domain.lineup.LineupSpot;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface LineupSpotRepository extends JpaRepository<LineupSpot, Long> {

    @Query(
            "SELECT ls FROM LineupSpot ls"
                    + " LEFT JOIN FETCH ls.player p LEFT JOIN FETCH p.user"
                    + " LEFT JOIN FETCH ls.guest"
                    + " WHERE ls.lineup.id = :lineupId")
    List<LineupSpot> findSpotsByLineupId(@Param("lineupId") Long lineupId);

    @Query(
            "SELECT ls FROM LineupSpot ls"
                    + " LEFT JOIN FETCH ls.player p LEFT JOIN FETCH p.user"
                    + " LEFT JOIN FETCH ls.guest"
                    + " WHERE ls.lineup.match.id = :matchId")
    List<LineupSpot> findSpotsByMatchId(@Param("matchId") Long matchId);

    @Modifying(clearAutomatically = true)
    @Query("DELETE FROM LineupSpot ls WHERE ls.lineup.id = :lineupId")
    void deleteByLineup_Id(@Param("lineupId") Long lineupId);
}
