package com.matchlog.be.repository;

import com.matchlog.be.constant.notification.NotificationType;
import com.matchlog.be.domain.notification.Notification;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByUser_IdAndIsReadFalseOrderByCreatedAtDesc(Long userId);

    List<Notification> findByUser_IdOrderByCreatedAtDesc(Long userId);

    long countByUser_IdAndIsReadFalse(Long userId);

    boolean existsByReferenceIdAndType(Long referenceId, NotificationType type);

    @Modifying
    @Query(
            "UPDATE Notification n SET n.isRead = true WHERE n.user.id = :userId AND n.isRead = false")
    void markAllAsReadByUserId(@Param("userId") Long userId);
}
