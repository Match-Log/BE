package com.matchlog.be.util.notification;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SseNotificationSender implements NotificationSender {

    private final SseEmitterManager sseEmitterManager;

    @Override
    public void send(Long userId, NotificationPayload payload) {
        sseEmitterManager.send(userId, payload);
    }
}
