package com.matchlog.be.dto.stat.response;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MatchStatListResponseDto {

    private Long matchId;
    private List<PlayerStatItemResponseDto> stats;
}
