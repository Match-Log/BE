package com.matchlog.be.dto.stat.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.matchlog.be.domain.stat.PlayerStat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PlayerStatItemResponseDto {

    private Long playerId;
    private String name;
    private String position;
    private Integer goals;
    private Integer assists;
    private Integer saves;

    public static PlayerStatItemResponseDto from(PlayerStat stat, String position) {
        return PlayerStatItemResponseDto.builder()
                .playerId(stat.getPlayer().getId())
                .name(stat.getPlayer().getUser().getName())
                .position(position)
                .goals(stat.getGoals())
                .assists(stat.getAssists())
                .saves(stat.getSaves())
                .build();
    }
}
