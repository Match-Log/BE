package com.matchlog.be.controller;

import com.matchlog.be.dto.notification.response.NotificationResponseDto;
import com.matchlog.be.dto.notification.response.UnreadCountResponseDto;
import com.matchlog.be.service.notification.NotificationService;
import com.matchlog.be.util.notification.SseEmitterManager;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;
    private final SseEmitterManager sseEmitterManager;

    @GetMapping(value = "/subscribe", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribe(@AuthenticationPrincipal Long userId) {
        SseEmitter emitter = sseEmitterManager.connect(userId);
        notificationService.flushPending(userId);
        return emitter;
    }

    @GetMapping
    public List<NotificationResponseDto> getAll(@AuthenticationPrincipal Long userId) {
        return notificationService.getAll(userId);
    }

    @GetMapping("/unread-count")
    public UnreadCountResponseDto getUnreadCount(@AuthenticationPrincipal Long userId) {
        return notificationService.countUnread(userId);
    }

    @PatchMapping("/{notificationId}/read")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void markAsRead(
            @AuthenticationPrincipal Long userId, @PathVariable Long notificationId) {
        notificationService.markAsRead(userId, notificationId);
    }

    @PatchMapping("/read-all")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void markAllAsRead(@AuthenticationPrincipal Long userId) {
        notificationService.markAllAsRead(userId);
    }
}
