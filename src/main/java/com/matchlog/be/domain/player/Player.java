package com.matchlog.be.domain.player;

import com.matchlog.be.constant.lineup.Position;
import com.matchlog.be.constant.player.Career;
import com.matchlog.be.constant.player.PreferredFoot;
import com.matchlog.be.domain.common.BaseTimeEntity;
import com.matchlog.be.domain.user.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.LocalDate;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "PLAYER")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class Player extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "userId", nullable = false, unique = true)
    private User user;

    private LocalDate birthDate;

    private Integer height;

    private Integer weight;

    @Enumerated(EnumType.STRING)
    @Column(length = 5)
    private PreferredFoot preferredFoot;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private Career career;

    private Integer yearsOfExperience;

    @Enumerated(EnumType.STRING)
    @Column(length = 10)
    private Position preferredPosition;

    @Enumerated(EnumType.STRING)
    @Column(length = 10)
    private Position subPosition;

    public static Player create(
            User user,
            LocalDate birthDate,
            Integer height,
            Integer weight,
            PreferredFoot preferredFoot,
            Career career,
            Integer yearsOfExperience,
            Position preferredPosition,
            Position subPosition) {
        return Player.builder()
                .user(user)
                .birthDate(birthDate)
                .height(height)
                .weight(weight)
                .preferredFoot(preferredFoot)
                .career(career)
                .yearsOfExperience(yearsOfExperience)
                .preferredPosition(preferredPosition)
                .subPosition(subPosition)
                .build();
    }

    public void updateProfile(
            LocalDate birthDate,
            Integer height,
            Integer weight,
            PreferredFoot preferredFoot,
            Career career,
            Integer yearsOfExperience,
            Position preferredPosition,
            Position subPosition) {
        if (birthDate != null) {
            this.birthDate = birthDate;
        }
        if (height != null) {
            this.height = height;
        }
        if (weight != null) {
            this.weight = weight;
        }
        if (preferredFoot != null) {
            this.preferredFoot = preferredFoot;
        }
        if (career != null) {
            this.career = career;
        }
        if (yearsOfExperience != null) {
            this.yearsOfExperience = yearsOfExperience;
        }
        if (preferredPosition != null) {
            this.preferredPosition = preferredPosition;
        }
        if (subPosition != null) {
            this.subPosition = subPosition;
        }
    }
}
