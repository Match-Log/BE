package com.matchlog.be.dto.notification.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.matchlog.be.constant.notification.NotificationType;
import com.matchlog.be.domain.notification.Notification;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class NotificationResponseDto {

    private Long id;
    private NotificationType type;
    private String title;
    private String body;

    @JsonProperty("isRead")
    private boolean isRead;

    private Long referenceId;

    private Long teamId;

    @JsonFormat(
            shape = JsonFormat.Shape.STRING,
            pattern = "yyyy-MM-dd'T'HH:mm:ss",
            timezone = "Asia/Seoul")
    private LocalDateTime createdAt;

    public static NotificationResponseDto from(Notification notification) {
        return NotificationResponseDto.builder()
                .id(notification.getId())
                .type(notification.getType())
                .title(notification.getTitle())
                .body(notification.getBody())
                .isRead(notification.isRead())
                .referenceId(notification.getReferenceId())
                .teamId(notification.getTeamId())
                .createdAt(notification.getCreatedAt())
                .build();
    }
}
