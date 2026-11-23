package com.matchlog.be.util.notification;

import com.matchlog.be.constant.notification.NotificationType;
import com.matchlog.be.domain.notification.Notification;

public record NotificationPayload(
        Long notificationId,
        NotificationType type,
        String title,
        String body,
        Long referenceId,
        Long teamId) {

    public static NotificationPayload from(Notification notification) {
        return new NotificationPayload(
                notification.getId(),
                notification.getType(),
                notification.getTitle(),
                notification.getBody(),
                notification.getReferenceId(),
                notification.getTeamId());
    }
}
