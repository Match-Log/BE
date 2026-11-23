package com.matchlog.be.util.notification;

public interface NotificationSender {

    void send(Long userId, NotificationPayload payload);
}
