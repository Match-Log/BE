package com.matchlog.be.repository;

import com.matchlog.be.domain.tactic.PersonalTactic;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PersonalTacticRepository extends JpaRepository<PersonalTactic, Long> {

    // [GET /api/v1/matches/{matchId}/tactics?scope=player&playerId={playerId}] 개인 전술 단건 조회 / [PUT]
    // upsert 기존 조회
    Optional<PersonalTactic> findByMatch_IdAndPlayer_Id(Long matchId, Long playerId);

    // [PUT /api/v1/matches/{matchId}/tactics?scope=player&playerId={playerId}] 개인 전술 존재 여부 (insert
    // vs update 분기)
    // → 제거: findByMatch_IdAndPlayer_Id 가 Optional 을 반환하므로 isPresent() 로 분기 가능. existsBy 를 별도 호출하면
    // DB 쿼리가 두 번 발생함.
    // boolean existsByMatch_IdAndPlayer_Id(Long matchId, Long playerId);

    // [GET /api/v1/matches/{matchId}/tactics] scope 생략 시 전체 조회 중 개인 전술 부분 담당
    // (TeamTacticRepository.findByMatch_Id 와 합산)
    List<PersonalTactic> findByMatch_Id(Long matchId);
}
