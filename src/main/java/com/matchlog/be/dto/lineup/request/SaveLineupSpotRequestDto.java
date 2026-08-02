package com.matchlog.be.dto.lineup.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SaveLineupSpotRequestDto {

    private Long playerId;
    private String position;
    private boolean isStarter;
}
