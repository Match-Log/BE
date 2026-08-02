package com.matchlog.be.domain.lineup;

import com.matchlog.be.domain.player.Player;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "LINEUP_SPOT")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class LineupSpot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lineupId", nullable = false)
    private Lineup lineup;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "playerId", nullable = false)
    private Player player;

    @Column(nullable = false, length = 10)
    private String position;

    @Column(nullable = false)
    private boolean isStarter;

    public static LineupSpot create(Lineup lineup, Player player, String position, boolean isStarter) {
        return LineupSpot.builder()
                .lineup(lineup)
                .player(player)
                .position(position)
                .isStarter(isStarter)
                .build();
    }
}
