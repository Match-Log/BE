package com.matchlog.be.domain.notification;

import com.matchlog.be.constant.notification.DeviceType;
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
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Getter
@Table(
        name = "USER_DEVICE_TOKEN",
        uniqueConstraints =
                @UniqueConstraint(
                        name = "uk_device_token_user_device",
                        columnNames = {"userId", "deviceType"}))
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class UserDeviceToken extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "userId", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User user;

    @Column(nullable = false, length = 500)
    private String fcmToken;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private DeviceType deviceType;

    public static UserDeviceToken create(User user, String fcmToken, DeviceType deviceType) {
        return UserDeviceToken.builder()
                .user(user)
                .fcmToken(fcmToken)
                .deviceType(deviceType)
                .build();
    }

    public void updateToken(String fcmToken) {
        this.fcmToken = fcmToken;
    }
}
