package com.matchlog.be.repository;

import com.matchlog.be.domain.feedback.PersonalFeedback;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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

    // [GET /api/v1/teams/{teamId}/players/{playerId}/stats/summary] 평점 평균 — isVisible 무관 전체 rating
    // 평균(비공개 피드백 기능 자체가 아직 없어 필터 불필요, rating이 null인 행은 AVG에서 자동 제외)
    @Query(
            "SELECT AVG(pf.rating) FROM PersonalFeedback pf JOIN pf.match m "
                    + "WHERE pf.player.id = :playerId AND m.team.id = :teamId "
                    + "AND m.matchDate >= :from AND m.matchDate < :to")
    Double averageRatingByPlayerAndPeriod(
            @Param("teamId") Long teamId,
            @Param("playerId") Long playerId,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to);
}
