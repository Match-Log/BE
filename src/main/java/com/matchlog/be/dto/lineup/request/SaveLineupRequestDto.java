package com.matchlog.be.dto.lineup.request;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SaveLineupRequestDto {

    private Long matchId;
    private int quarter;
    private String formation;
    private List<SaveLineupSpotRequestDto> spots;
}
