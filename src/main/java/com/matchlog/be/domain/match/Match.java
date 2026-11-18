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

    @Column(length = 255)
    private String locationAddress;

    private Double latitude;

    private Double longitude;

    @Column(length = 30)
    private String kakaoPlaceId;

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

    @Column(length = 500)
    private String memo;

    public static Match create(
            Team team,
            String opponent,
            LocalDateTime matchDate,
            String location,
            String locationAddress,
            Double latitude,
            Double longitude,
            String kakaoPlaceId,
            HomeAway homeAway,
            MatchType matchType,
            String memo) {
        return Match.builder()
                .team(team)
                .opponent(opponent)
                .matchDate(matchDate)
                .location(location)
                .locationAddress(locationAddress)
                .latitude(latitude)
                .longitude(longitude)
                .kakaoPlaceId(kakaoPlaceId)
                .homeAway(homeAway)
                .matchType(matchType)
                .isFinished(false)
                .memo(memo)
                .build();
    }

    public void updateSchedule(
            String opponent,
            LocalDateTime matchDate,
            HomeAway homeAway,
            MatchType matchType,
            String memo) {
        if (opponent != null) {
            this.opponent = opponent;
        }
        if (matchDate != null) {
            this.matchDate = matchDate;
        }
        if (homeAway != null) {
            this.homeAway = homeAway;
        }
        if (matchType != null) {
            this.matchType = matchType;
        }
        if (memo != null) {
            this.memo = memo;
        }
    }

    /** 장소 정보를 그룹 단위로 통째로 덮어쓴다. 검색 장소 → 직접 입력 장소로 바뀌는 경우 좌표/장소ID가 남지 않도록 부분 갱신하지 않는다. */
    public void changeLocation(
            String location,
            String locationAddress,
            Double latitude,
            Double longitude,
            String kakaoPlaceId) {
        this.location = location;
        this.locationAddress = locationAddress;
        this.latitude = latitude;
        this.longitude = longitude;
        this.kakaoPlaceId = kakaoPlaceId;
    }

    public void recordScore(Integer scoreHome, Integer scoreAway) {
        if (scoreHome != null) {
            this.scoreHome = scoreHome;
        }
        if (scoreAway != null) {
            this.scoreAway = scoreAway;
        }
    }

    public void changeStatusAsFinished() {
        this.isFinished = true;
    }

    public boolean isVoteDeadlinePassed() {
        return LocalDateTime.now().isAfter(this.matchDate.minusHours(1));
    }
}
