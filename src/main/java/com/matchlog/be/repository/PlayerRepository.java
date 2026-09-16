package com.matchlog.be.repository;

import com.matchlog.be.domain.player.Player;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PlayerRepository extends JpaRepository<Player, Long> {

    // [전역] JWT userId → Player 변환. 대부분의 API에서 현재 선수 특정 시 사용.
    Optional<Player> findByUser_Id(Long userId);

    // [POST /api/v1/players] 선수 등록 전 Player 중복 생성 차단
    boolean existsByUser_Id(Long userId);

    // [GET /api/v1/matches/{matchId}/mvp-votes] 득표자 이름 표시용 — Player → User fetch join (N+1 방지)
    @Query("SELECT p FROM Player p JOIN FETCH p.user WHERE p.id IN :ids")
    List<Player> findAllWithUserByIdIn(@Param("ids") Collection<Long> ids);
}
