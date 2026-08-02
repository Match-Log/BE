package com.matchlog.be.dto.participation.response;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.matchlog.be.domain.participation.Participation;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AssignKickerResponseDto {

    private Long teamId;
    private Long playerId;
    private boolean isCaptain;
    private boolean isPkTaker;
    private boolean isFkRight;
    private boolean isFkLeft;
    private boolean isCkRight;
    private boolean isCkLeft;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "Asia/Seoul")
    private LocalDateTime updatedAt;

    public static AssignKickerResponseDto from(Participation participation) {
        return AssignKickerResponseDto.builder()
                .teamId(participation.getTeam().getId())
                .playerId(participation.getPlayer().getId())
                .isCaptain(participation.isCaptain())
                .isPkTaker(participation.isPkTaker())
                .isFkRight(participation.isFkRight())
                .isFkLeft(participation.isFkLeft())
                .isCkRight(participation.isCkRight())
                .isCkLeft(participation.isCkLeft())
                .updatedAt(participation.getUpdatedAt())
                .build();
    }
}
