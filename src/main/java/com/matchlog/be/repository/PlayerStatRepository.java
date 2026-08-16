package com.matchlog.be.repository;

import com.matchlog.be.domain.stat.PlayerStat;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PlayerStatRepository extends JpaRepository<PlayerStat, Long> {

    // [PUT /api/v1/matches/{matchId}/stats/players/{playerId}] 스탯 upsert 시 기존 스탯 조회 / [GET] 선수 단건
    // 스탯 조회
    Optional<PlayerStat> findByMatch_IdAndPlayer_Id(Long matchId, Long playerId);

    // [PUT /api/v1/matches/{matchId}/stats/players/{playerId}] 스탯 존재 여부 (insert vs update 분기)
    // → 제거: findByMatch_IdAndPlayer_Id 가 Optional 을 반환하므로 isPresent() 로 분기 가능. existsBy 를 별도 호출하면
    // DB 쿼리가 두 번 발생함.
    // boolean existsByMatch_IdAndPlayer_Id(Long matchId, Long playerId);

    // [GET /api/v1/matches/{matchId}/stats] 경기 전체 스탯 조회 — PlayerStat → Player → User fetch join
    // 규칙§3: 스탯 화면에 선수 이름·프로필 이미지 필요.
    @Query(
            "SELECT ps FROM PlayerStat ps JOIN FETCH ps.player pl JOIN FETCH pl.user WHERE ps.match.id = :matchId")
    List<PlayerStat> findStatsByMatchId(@Param("matchId") Long matchId);

    // 대응 API 없음: 선수 시즌 누적 스탯 엔드포인트가 API 문서에 존재하지 않음. 추후 API 추가 시 복구.
    // List<PlayerStat> findByPlayer_Id(Long playerId);
}
