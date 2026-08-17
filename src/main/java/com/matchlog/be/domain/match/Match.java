package com.matchlog.be.domain.match;

import com.matchlog.be.constant.match.HomeAway;
import com.matchlog.be.constant.match.MatchType;
import com.matchlog.be.domain.common.BaseTimeEntity;
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
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Getter
@Table(name = "`MATCH`")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class Match extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "teamId", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Team team;

    @Column(nullable = false, length = 100)
    private String opponent;

    @Column(nullable = false)
    private LocalDateTime matchDate;

    @Column(length = 200)
    private String location;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private HomeAway homeAway;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MatchType matchType;

    private Integer scoreHome;

    private Integer scoreAway;

    @Column(nullable = false)
    @Builder.Default
    private boolean isFinished = false;

    public static Match create(
            Team team,
            String opponent,
            LocalDateTime matchDate,
            String location,
            HomeAway homeAway,
            MatchType matchType) {
        return Match.builder()
                .team(team)
                .opponent(opponent)
                .matchDate(matchDate)
                .location(location)
                .homeAway(homeAway)
                .matchType(matchType)
                .isFinished(false)
                .build();
    }

    public void updateSchedule(
            String opponent, LocalDateTime matchDate, String location, HomeAway homeAway) {
        if (opponent != null) {
            this.opponent = opponent;
        }
        if (matchDate != null) {
            this.matchDate = matchDate;
        }
        if (location != null) {
            this.location = location;
        }
        if (homeAway != null) {
            this.homeAway = homeAway;
        }
    }

    public void recordScore(Integer scoreHome, Integer scoreAway) {
        this.scoreHome = scoreHome;
        this.scoreAway = scoreAway;
    }

    public void changeStatusAsFinished() {
        this.isFinished = true;
    }
}
