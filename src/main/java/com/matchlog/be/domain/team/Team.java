package com.matchlog.be.domain.team;

import com.matchlog.be.domain.common.BaseTimeEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "TEAM")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class Team extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 500)
    private String teamImage;

    @Column(length = 100)
    private String region;

    private Integer foundedYear;

    @Column(length = 200)
    private String homeGround;

    @Column(nullable = false, unique = true, length = 6)
    private String inviteCode;

    public static Team create(String name, String teamImage, String region, Integer foundedYear,
            String homeGround, String inviteCode) {
        return Team.builder()
                .name(name)
                .teamImage(teamImage)
                .region(region)
                .foundedYear(foundedYear)
                .homeGround(homeGround)
                .inviteCode(inviteCode)
                .build();
    }

    public void changeInfo(String name, String teamImage, String region, Integer foundedYear, String homeGround) {
        if (name != null) {
            this.name = name;
        }
        if (teamImage != null) {
            this.teamImage = teamImage;
        }
        if (region != null) {
            this.region = region;
        }
        if (foundedYear != null) {
            this.foundedYear = foundedYear;
        }
        if (homeGround != null) {
            this.homeGround = homeGround;
        }
    }
}
