package com.matchlog.be.domain.tactic;

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
@Table(name = "PERSONAL_TACTIC")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class PersonalTactic extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "matchId", nullable = false)
    private Match match;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "playerId", nullable = false)
    private Player player;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "coachId", nullable = false)
    private Player coach;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    public static PersonalTactic create(Match match, Player player, Player coach, String content) {
        return PersonalTactic.builder()
                .match(match)
                .player(player)
                .coach(coach)
                .content(content)
                .build();
    }

    public void updateContent(String content) {
        this.content = content;
    }
}
