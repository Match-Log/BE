package com.matchlog.be.repository;

import com.matchlog.be.domain.participation.Participation;
import com.matchlog.be.constant.participation.ParticipationRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ParticipationRepository extends JpaRepository<Participation, Long> {

    Optional<Participation> findByTeam_IdAndPlayer_Id(Long teamId, Long playerId);

    boolean existsByTeam_IdAndPlayer_Id(Long teamId, Long playerId);

    long countByPlayer_Id(Long playerId);

    long countByTeam_IdAndRole(Long teamId, ParticipationRole role);

    @Query("SELECT p FROM Participation p JOIN FETCH p.player pl JOIN FETCH pl.user WHERE p.team.id = :teamId")
    List<Participation> findRosterByTeamId(@Param("teamId") Long teamId);
}
