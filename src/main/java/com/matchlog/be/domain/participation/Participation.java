package com.matchlog.be.domain.participation;

import com.matchlog.be.constant.participation.ParticipationRole;
import com.matchlog.be.domain.player.Player;
import com.matchlog.be.domain.team.Team;
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
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "PARTICIPATIONS")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class Participation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "playerId", nullable = false)
    private Player player;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "teamId", nullable = false)
    private Team team;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ParticipationRole role;

    private Integer number;

    @Column(length = 10)
    private String mainPosition;

    @Column(length = 10)
    private String subPosition;

    @Column(nullable = false)
    private LocalDateTime joinedAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Column(nullable = false)
    @Builder.Default
    private boolean isCaptain = false;

    @Column(nullable = false)
    @Builder.Default
    private boolean isPkTaker = false;

    @Column(nullable = false)
    @Builder.Default
    private boolean isFkRight = false;

    @Column(nullable = false)
    @Builder.Default
    private boolean isFkLeft = false;

    @Column(nullable = false)
    @Builder.Default
    private boolean isCkRight = false;

    @Column(nullable = false)
    @Builder.Default
    private boolean isCkLeft = false;

    public static Participation create(Player player, Team team, ParticipationRole role) {
        LocalDateTime now = LocalDateTime.now();
        return Participation.builder()
                .player(player)
                .team(team)
                .role(role)
                .joinedAt(now)
                .updatedAt(now)
                .build();
    }

    public void changeRole(ParticipationRole role) {
        this.role = role;
        touch();
    }

    public void changePosition(Integer number, String mainPosition, String subPosition) {
        this.number = number;
        this.mainPosition = mainPosition;
        this.subPosition = subPosition;
        touch();
    }

    public void assignKickerRoles(
            boolean isCaptain,
            boolean isPkTaker,
            boolean isFkRight,
            boolean isFkLeft,
            boolean isCkRight,
            boolean isCkLeft) {
        this.isCaptain = isCaptain;
        this.isPkTaker = isPkTaker;
        this.isFkRight = isFkRight;
        this.isFkLeft = isFkLeft;
        this.isCkRight = isCkRight;
        this.isCkLeft = isCkLeft;
        touch();
    }

    private void touch() {
        this.updatedAt = LocalDateTime.now();
    }
}
