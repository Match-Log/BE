package com.matchlog.be.domain.user;

import com.matchlog.be.constant.user.Provider;
import com.matchlog.be.domain.common.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
@Table(name = "`USER`")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class User extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(length = 255)
    private String password;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(length = 500)
    private String profileImage;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private Provider provider;

    public static User create(
            String email, String password, String name, String profileImage, Provider provider) {
        return User.builder()
                .email(email)
                .password(password)
                .name(name)
                .profileImage(profileImage)
                .provider(provider)
                .build();
    }

    public void changeProfile(String name, String profileImage) {
        if (name != null) {
            this.name = name;
        }
        if (profileImage != null) {
            this.profileImage = profileImage;
        }
    }
}
