package com.matchlog.be.repository;

import com.matchlog.be.domain.tactic.TeamTactic;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TeamTacticRepository extends JpaRepository<TeamTactic, Long> {

    Optional<TeamTactic> findByMatch_Id(Long matchId);

    boolean existsByMatch_Id(Long matchId);
}
