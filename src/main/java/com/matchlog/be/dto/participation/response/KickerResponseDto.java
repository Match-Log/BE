package com.matchlog.be.dto.participation.response;

import com.matchlog.be.domain.participation.Participation;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class KickerResponseDto {

    private Long teamId;
    private Long playerId;
    private String name;
    private boolean isCaptain;
    private boolean isPkTaker;
    private boolean isFkRight;
    private boolean isFkLeft;
    private boolean isCkRight;
    private boolean isCkLeft;

    public static KickerResponseDto from(Participation participation) {
        return KickerResponseDto.builder()
                .teamId(participation.getTeam().getId())
                .playerId(participation.getPlayer().getId())
                .name(participation.getPlayer().getUser().getName())
                .isCaptain(participation.isCaptain())
                .isPkTaker(participation.isPkTaker())
                .isFkRight(participation.isFkRight())
                .isFkLeft(participation.isFkLeft())
                .isCkRight(participation.isCkRight())
                .isCkLeft(participation.isCkLeft())
                .build();
    }
}
