package com.matchlog.be.domain.stat;

import com.matchlog.be.domain.common.BaseTimeEntity;
import com.matchlog.be.domain.match.Match;
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
@Table(name = "PLAYER_STAT")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class PlayerStat extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "matchId", nullable = false)
    private Match match;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "playerId", nullable = false)
    private Player player;

    @Column(nullable = false)
    @Builder.Default
    private int goals = 0;

    @Column(nullable = false)
    @Builder.Default
    private int assists = 0;

    @Column(nullable = false)
    @Builder.Default
    private int shots = 0;

    private Integer saves;

    private Integer goalsConceded;

    private Boolean cleanSheet;

    public static PlayerStat create(Match match, Player player) {
        return PlayerStat.builder()
                .match(match)
                .player(player)
                .build();
    }

    public void updateStats(int goals, int assists, int shots, Integer saves, Integer goalsConceded,
            Boolean cleanSheet) {
        this.goals = goals;
        this.assists = assists;
        this.shots = shots;
        this.saves = saves;
        this.goalsConceded = goalsConceded;
        this.cleanSheet = cleanSheet;
    }
}
