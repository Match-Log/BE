package com.matchlog.be.repository;

import com.matchlog.be.domain.player.Player;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PlayerRepository extends JpaRepository<Player, Long> {

    // [전역] JWT userId → Player 변환. 대부분의 API에서 현재 선수 특정 시 사용.
    Optional<Player> findByUser_Id(Long userId);

    // [POST /api/v1/players] 선수 등록 전 Player 중복 생성 차단
    boolean existsByUser_Id(Long userId);
}
