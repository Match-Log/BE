package com.matchlog.be.dto.lineup.response;

import com.matchlog.be.domain.lineup.LineupSpot;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LineupSpotResponseDto {

    private Long spotId;
    private Long playerId;
    private String name;
    private String position;
    private boolean isStarter;

    public static LineupSpotResponseDto from(LineupSpot spot) {
        return LineupSpotResponseDto.builder()
                .spotId(spot.getId())
                .playerId(spot.getPlayer().getId())
                .name(spot.getPlayer().getUser().getName())
                .position(spot.getPosition())
                .isStarter(spot.isStarter())
                .build();
    }
}
