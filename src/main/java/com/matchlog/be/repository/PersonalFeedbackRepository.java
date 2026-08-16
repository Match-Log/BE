package com.matchlog.be.repository;

import com.matchlog.be.domain.feedback.PersonalFeedback;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PersonalFeedbackRepository extends JpaRepository<PersonalFeedback, Long> {

    // [GET /api/v1/matches/{matchId}/feedbacks?scope=player&playerId={playerId}] 개인 피드백 단건 조회 /
    // [PUT] upsert 기존 조회
    Optional<PersonalFeedback> findByMatch_IdAndPlayer_Id(Long matchId, Long playerId);

    // [PUT /api/v1/matches/{matchId}/feedbacks?scope=player&playerId={playerId}] 개인 피드백 존재 여부
    // (insert vs update 분기)
    // → 제거: findByMatch_IdAndPlayer_Id 가 Optional 을 반환하므로 isPresent() 로 분기 가능. existsBy 를 별도 호출하면
    // DB 쿼리가 두 번 발생함.
    // boolean existsByMatch_IdAndPlayer_Id(Long matchId, Long playerId);

    // [GET /api/v1/matches/{matchId}/feedbacks] scope 생략 시 전체 조회 중 개인 피드백 부분 담당
    // (TeamFeedbackRepository.findByMatch_Id 와 합산)
    List<PersonalFeedback> findByMatch_Id(Long matchId);
}
