package com.matchlog.be.domain.tactic;

import com.matchlog.be.constant.tactic.CrossType;
import com.matchlog.be.constant.tactic.DefensiveLineHeight;
import com.matchlog.be.constant.tactic.DefensiveSpacing;
import com.matchlog.be.constant.tactic.FlankDefense;
import com.matchlog.be.constant.tactic.PossessionStrategy;
import com.matchlog.be.constant.tactic.PressingTrigger;
import com.matchlog.be.constant.tactic.SetPieceAttack;
import com.matchlog.be.constant.tactic.SetPieceDefense;
import com.matchlog.be.domain.common.BaseTimeEntity;
import com.matchlog.be.domain.match.Match;
import com.matchlog.be.domain.player.Player;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
@Table(name = "TEAM_TACTIC")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class TeamTactic extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "matchId", nullable = false)
    private Match match;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "coachId", nullable = true)
    private Player coach;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private DefensiveLineHeight defensiveLineHeight;

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private DefensiveSpacing defensiveSpacing;

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private SetPieceDefense setPieceDefense;

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private FlankDefense flankDefense;

    @Column private Boolean offsideTrap;

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private CrossType crossType;

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private SetPieceAttack setPieceAttack;

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private PossessionStrategy possessionStrategy;

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private PressingTrigger pressingTrigger;

    @Column private Integer pressingIntensity;

    public static TeamTactic create(
            Match match,
            Player coach,
            String content,
            DefensiveLineHeight defensiveLineHeight,
            DefensiveSpacing defensiveSpacing,
            SetPieceDefense setPieceDefense,
            FlankDefense flankDefense,
            Boolean offsideTrap,
            CrossType crossType,
            SetPieceAttack setPieceAttack,
            PossessionStrategy possessionStrategy,
            PressingTrigger pressingTrigger,
            Integer pressingIntensity) {
        return TeamTactic.builder()
                .match(match)
                .coach(coach)
                .content(content)
                .defensiveLineHeight(defensiveLineHeight)
                .defensiveSpacing(defensiveSpacing)
                .setPieceDefense(setPieceDefense)
                .flankDefense(flankDefense)
                .offsideTrap(offsideTrap)
                .crossType(crossType)
                .setPieceAttack(setPieceAttack)
                .possessionStrategy(possessionStrategy)
                .pressingTrigger(pressingTrigger)
                .pressingIntensity(pressingIntensity)
                .build();
    }

    public void update(
            String content,
            DefensiveLineHeight defensiveLineHeight,
            DefensiveSpacing defensiveSpacing,
            SetPieceDefense setPieceDefense,
            FlankDefense flankDefense,
            Boolean offsideTrap,
            CrossType crossType,
            SetPieceAttack setPieceAttack,
            PossessionStrategy possessionStrategy,
            PressingTrigger pressingTrigger,
            Integer pressingIntensity) {
        this.content = content;
        this.defensiveLineHeight = defensiveLineHeight;
        this.defensiveSpacing = defensiveSpacing;
        this.setPieceDefense = setPieceDefense;
        this.flankDefense = flankDefense;
        this.offsideTrap = offsideTrap;
        this.crossType = crossType;
        this.setPieceAttack = setPieceAttack;
        this.possessionStrategy = possessionStrategy;
        this.pressingTrigger = pressingTrigger;
        this.pressingIntensity = pressingIntensity;
    }
}
