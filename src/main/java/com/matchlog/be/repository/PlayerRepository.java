package com.matchlog.be.repository;

import com.matchlog.be.domain.player.Player;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PlayerRepository extends JpaRepository<Player, Long> {

    Optional<Player> findByUser_Id(Long userId);

    boolean existsByUser_Id(Long userId);
}
