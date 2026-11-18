package com.matchlog.be.domain.match;

import com.matchlog.be.constant.match.HomeAway;
import com.matchlog.be.constant.match.MatchType;
import com.matchlog.be.domain.common.BaseTimeEntity;
import com.matchlog.be.domain.team.Team;
import com.matchlog.be.exception.CustomException;
import com.matchlog.be.exception.constant.MatchErrorCode;
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

    private static final String SCHEDULE_LOCKED_MESSAGE = "종료된 경기는 일정 정보를 수정할 수 없습니다.";

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
        if (opponent != null || matchDate != null || homeAway != null || matchType != null) {
            requireNotFinished(SCHEDULE_LOCKED_MESSAGE);
        }
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
        requireNotFinished(SCHEDULE_LOCKED_MESSAGE);
        this.location = location;
        this.locationAddress = locationAddress;
        this.latitude = latitude;
        this.longitude = longitude;
        this.kakaoPlaceId = kakaoPlaceId;
    }

    /** 종료된 경기의 스코어 정정. 종료 전 경기는 finish로만 스코어를 기록한다. */
    public void recordScore(Integer scoreHome, Integer scoreAway) {
        requireFinished("종료된 경기에만 스코어를 입력할 수 있습니다.");
        applyScore(scoreHome, scoreAway);
    }

    /** 경기 시작 시각이 지났고 홈/어웨이 스코어가 모두 있어야 종료할 수 있다. */
    public void finish(Integer scoreHome, Integer scoreAway) {
        requireNotFinished();
        if (LocalDateTime.now().isBefore(this.matchDate)) {
            throw new CustomException(MatchErrorCode.MATCH_NOT_STARTED);
        }
        Integer home = scoreHome != null ? scoreHome : this.scoreHome;
        Integer away = scoreAway != null ? scoreAway : this.scoreAway;
        if (home == null || away == null) {
            throw new CustomException(MatchErrorCode.MATCH_SCORE_REQUIRED);
        }
        applyScore(home, away);
        this.isFinished = true;
    }

    private void applyScore(Integer scoreHome, Integer scoreAway) {
        if (scoreHome != null) {
            this.scoreHome = scoreHome;
        }
        if (scoreAway != null) {
            this.scoreAway = scoreAway;
        }
    }

    /** 종료 후 단계(스탯, 피드백, MVP 등) 진입 전 검증. */
    public void requireFinished() {
        if (!this.isFinished) {
            throw new CustomException(MatchErrorCode.MATCH_NOT_FINISHED);
        }
    }

    public void requireFinished(String message) {
        if (!this.isFinished) {
            throw new CustomException(MatchErrorCode.MATCH_NOT_FINISHED, message);
        }
    }

    /** 종료 전 단계(투표, 전술 등) 진입 전 검증. */
    public void requireNotFinished() {
        if (this.isFinished) {
            throw new CustomException(MatchErrorCode.MATCH_ALREADY_FINISHED);
        }
    }

    public void requireNotFinished(String message) {
        if (this.isFinished) {
            throw new CustomException(MatchErrorCode.MATCH_ALREADY_FINISHED, message);
        }
    }

    public boolean isVoteDeadlinePassed() {
        return LocalDateTime.now().isAfter(this.matchDate.minusHours(1));
    }
}
