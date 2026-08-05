package com.matchlog.be.repository;

import com.matchlog.be.domain.feedback.PersonalFeedback;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PersonalFeedbackRepository extends JpaRepository<PersonalFeedback, Long> {

    Optional<PersonalFeedback> findByMatch_IdAndPlayer_Id(Long matchId, Long playerId);

    boolean existsByMatch_IdAndPlayer_Id(Long matchId, Long playerId);

    List<PersonalFeedback> findByMatch_Id(Long matchId);
}
