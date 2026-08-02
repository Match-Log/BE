package com.matchlog.be.dto.stat.response;

import com.matchlog.be.domain.stat.PlayerStat;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PlayerStatResponseDto {

    private Long matchId;
    private Long playerId;
    private String name;
    private int goals;
    private int assists;
    private int shots;
    private Integer saves;
    private Integer goalsConceded;
    private Boolean cleanSheet;

    public static PlayerStatResponseDto from(PlayerStat stat) {
        return PlayerStatResponseDto.builder()
                .matchId(stat.getMatch().getId())
                .playerId(stat.getPlayer().getId())
                .name(stat.getPlayer().getUser().getName())
                .goals(stat.getGoals())
                .assists(stat.getAssists())
                .shots(stat.getShots())
                .saves(stat.getSaves())
                .goalsConceded(stat.getGoalsConceded())
                .cleanSheet(stat.getCleanSheet())
                .build();
    }
}
