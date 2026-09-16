package com.matchlog.be.domain.stat;

import com.matchlog.be.domain.common.BaseTimeEntity;
import com.matchlog.be.domain.match.Match;
import com.matchlog.be.domain.player.Player;
import jakarta.persistence.*;
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

    private Integer goals;

    private Integer assists;

    private Integer saves;

    private Integer goalsConceded;

    private Boolean cleanSheet;

    @Column(nullable = false)
    @Builder.Default
    private boolean isMvp = false;

    public static PlayerStat create(Match match, Player player) {
        return PlayerStat.builder().match(match).player(player).build();
    }

    public void updateStats(Integer goals, Integer assists, Integer saves) {
        this.goals = goals;
        this.assists = assists;
        this.saves = saves;
    }

    public void changeMvpStatus(boolean isMvp) {
        this.isMvp = isMvp;
    }
}
