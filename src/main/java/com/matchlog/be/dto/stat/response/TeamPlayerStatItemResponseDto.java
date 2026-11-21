package com.matchlog.be.dto.stat.response;

import com.matchlog.be.constant.lineup.Position;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TeamPlayerStatItemResponseDto {

    private Long playerId;
    private String name;
    private Position position;
    private long matchCount;
    private long goals;
    private long assists;
    private Long saves;
    private long cleanSheetCount;
    private long mvpCount;
}
