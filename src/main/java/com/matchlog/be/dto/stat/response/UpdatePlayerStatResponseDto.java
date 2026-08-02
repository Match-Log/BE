package com.matchlog.be.dto.stat.response;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.matchlog.be.domain.stat.PlayerStat;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UpdatePlayerStatResponseDto {

    private Long matchId;
    private Long playerId;
    private int goals;
    private int assists;
    private int shots;
    private Integer saves;
    private Integer goalsConceded;
    private Boolean cleanSheet;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "Asia/Seoul")
    private LocalDateTime updatedAt;

    public static UpdatePlayerStatResponseDto from(PlayerStat stat) {
        return UpdatePlayerStatResponseDto.builder()
                .matchId(stat.getMatch().getId())
                .playerId(stat.getPlayer().getId())
                .goals(stat.getGoals())
                .assists(stat.getAssists())
                .shots(stat.getShots())
                .saves(stat.getSaves())
                .goalsConceded(stat.getGoalsConceded())
                .cleanSheet(stat.getCleanSheet())
                .updatedAt(stat.getUpdatedAt())
                .build();
    }
}
