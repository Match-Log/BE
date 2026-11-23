package com.matchlog.be.service.notification;

import com.matchlog.be.constant.notification.NotificationType;
import com.matchlog.be.domain.notification.Notification;
import com.matchlog.be.domain.user.User;
import com.matchlog.be.dto.notification.response.NotificationResponseDto;
import com.matchlog.be.dto.notification.response.UnreadCountResponseDto;
import com.matchlog.be.exception.CustomException;
import com.matchlog.be.exception.constant.NotificationErrorCode;
import com.matchlog.be.repository.NotificationRepository;
import com.matchlog.be.util.notification.NotificationPayload;
import com.matchlog.be.util.notification.NotificationSender;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationSender notificationSender;

    @Transactional
    public void create(
            User user,
            NotificationType type,
            String title,
            String body,
            Long referenceId,
            Long teamId) {
        Notification notification =
                Notification.create(user, type, title, body, referenceId, teamId);
        notificationRepository.save(notification);
        notificationSender.send(user.getId(), NotificationPayload.from(notification));
    }

    @Transactional(readOnly = true)
    public void flushPending(Long userId) {
        notificationRepository
                .findByUser_IdAndIsReadFalseOrderByCreatedAtDesc(userId)
                .forEach(n -> notificationSender.send(userId, NotificationPayload.from(n)));
    }

    @Transactional(readOnly = true)
    public List<NotificationResponseDto> getAll(Long userId) {
        return notificationRepository.findByUser_IdOrderByCreatedAtDesc(userId).stream()
                .map(NotificationResponseDto::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public UnreadCountResponseDto countUnread(Long userId) {
        return new UnreadCountResponseDto(
                notificationRepository.countByUser_IdAndIsReadFalse(userId));
    }

    @Transactional
    public void markAsRead(Long userId, Long notificationId) {
        Notification notification =
                notificationRepository
                        .findById(notificationId)
                        .orElseThrow(
                                () ->
                                        new CustomException(
                                                NotificationErrorCode.NOTIFICATION_NOT_FOUND));

        if (!notification.getUser().getId().equals(userId)) {
            throw new CustomException(NotificationErrorCode.NOTIFICATION_FORBIDDEN);
        }

        notification.markAsRead();
    }

    @Transactional
    public void markAllAsRead(Long userId) {
        notificationRepository.markAllAsReadByUserId(userId);
    }
}
