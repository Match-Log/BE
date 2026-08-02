package com.matchlog.be.dto.participation.response;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.matchlog.be.constant.participation.ParticipationRole;
import com.matchlog.be.domain.participation.Participation;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UpdateParticipationResponseDto {

    private Long participationId;
    private Long teamId;
    private Long playerId;
    private ParticipationRole role;
    private Integer number;
    private String mainPosition;
    private String subPosition;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "Asia/Seoul")
    private LocalDateTime updatedAt;

    public static UpdateParticipationResponseDto from(Participation participation) {
        return UpdateParticipationResponseDto.builder()
                .participationId(participation.getId())
                .teamId(participation.getTeam().getId())
                .playerId(participation.getPlayer().getId())
                .role(participation.getRole())
                .number(participation.getNumber())
                .mainPosition(participation.getMainPosition())
                .subPosition(participation.getSubPosition())
                .updatedAt(participation.getUpdatedAt())
                .build();
    }
}
