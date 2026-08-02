package com.matchlog.be.domain.player;

import java.time.LocalDate;

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

    @Column(columnDefinition = "TEXT")
    private String career;

    public static Player create(User user, LocalDate birthDate, Integer height, Integer weight,
            PreferredFoot preferredFoot, String career) {
        return Player.builder()
                .user(user)
                .birthDate(birthDate)
                .height(height)
                .weight(weight)
                .preferredFoot(preferredFoot)
                .career(career)
                .build();
    }

    public void updateProfile(Integer height, Integer weight, PreferredFoot preferredFoot, String career) {
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
    }
}
