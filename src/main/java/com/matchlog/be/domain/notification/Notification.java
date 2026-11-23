package com.matchlog.be.domain.notification;

import com.matchlog.be.constant.notification.NotificationType;
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
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Getter
@Table(name = "NOTIFICATION")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class Notification extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "userId", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private NotificationType type;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(nullable = false, length = 300)
    private String body;

    @Column(nullable = false)
    @Builder.Default
    private boolean isRead = false;

    private Long referenceId;

    private Long teamId;

    public static Notification create(
            User user,
            NotificationType type,
            String title,
            String body,
            Long referenceId,
            Long teamId) {
        return Notification.builder()
                .user(user)
                .type(type)
                .title(title)
                .body(body)
                .isRead(false)
                .referenceId(referenceId)
                .teamId(teamId)
                .build();
    }

    public void markAsRead() {
        this.isRead = true;
    }
}
