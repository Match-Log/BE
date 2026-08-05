package com.matchlog.be.repository;

import com.matchlog.be.domain.match.Match;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface MatchRepository extends JpaRepository<Match, Long> {

    boolean existsByTeam_IdAndMatchDate(Long teamId, LocalDateTime matchDate);

    List<Match> findByTeam_Id(Long teamId);

    List<Match> findByTeam_IdAndIsFinished(Long teamId, boolean isFinished);
}
