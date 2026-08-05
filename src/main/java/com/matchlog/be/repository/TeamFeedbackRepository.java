package com.matchlog.be.repository;

import com.matchlog.be.domain.feedback.TeamFeedback;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TeamFeedbackRepository extends JpaRepository<TeamFeedback, Long> {

    Optional<TeamFeedback> findByMatch_Id(Long matchId);

    boolean existsByMatch_Id(Long matchId);
}
