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
public class PlayerStatResponseDto {

    private Long matchId;
    private Long playerId;
    private String name;
    private Integer goals;
    private Integer assists;
    private Integer saves;

    public static PlayerStatResponseDto from(PlayerStat stat) {
        return PlayerStatResponseDto.builder()
                .matchId(stat.getMatch().getId())
                .playerId(stat.getPlayer().getId())
                .name(stat.getPlayer().getUser().getName())
                .goals(stat.getGoals())
                .assists(stat.getAssists())
                .saves(stat.getSaves())
                .build();
    }

    public static PlayerStatResponseDto defaultOf(Long matchId, Long playerId, String name) {
        return PlayerStatResponseDto.builder()
                .matchId(matchId)
                .playerId(playerId)
                .name(name)
                .build();
    }
}
