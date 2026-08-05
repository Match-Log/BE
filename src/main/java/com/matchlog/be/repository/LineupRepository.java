package com.matchlog.be.repository;

import com.matchlog.be.domain.lineup.Lineup;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LineupRepository extends JpaRepository<Lineup, Long> {

    Optional<Lineup> findByMatch_IdAndQuarter(Long matchId, int quarter);

    boolean existsByMatch_IdAndQuarter(Long matchId, int quarter);

    List<Lineup> findByMatch_Id(Long matchId);
}
