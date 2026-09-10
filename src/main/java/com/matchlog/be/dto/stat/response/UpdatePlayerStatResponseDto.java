package com.matchlog.be.dto.stat.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.matchlog.be.domain.stat.PlayerStat;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UpdatePlayerStatResponseDto {

    private Long matchId;
    private Long playerId;
    private Integer goals;
    private Integer assists;
    private Integer saves;

    @JsonFormat(
            shape = JsonFormat.Shape.STRING,
            pattern = "yyyy-MM-dd'T'HH:mm:ss",
            timezone = "Asia/Seoul")
    private LocalDateTime updatedAt;

    public static UpdatePlayerStatResponseDto from(PlayerStat stat) {
        return UpdatePlayerStatResponseDto.builder()
                .matchId(stat.getMatch().getId())
                .playerId(stat.getPlayer().getId())
                .goals(stat.getGoals())
                .assists(stat.getAssists())
                .saves(stat.getSaves())
                .updatedAt(stat.getUpdatedAt())
                .build();
    }
}
