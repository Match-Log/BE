package com.matchlog.be.repository;

import com.matchlog.be.domain.tactic.PersonalTactic;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PersonalTacticRepository extends JpaRepository<PersonalTactic, Long> {

    Optional<PersonalTactic> findByMatch_IdAndPlayer_Id(Long matchId, Long playerId);

    boolean existsByMatch_IdAndPlayer_Id(Long matchId, Long playerId);

    List<PersonalTactic> findByMatch_Id(Long matchId);
}
