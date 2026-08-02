package com.matchlog.be.dto.lineup.response;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.matchlog.be.domain.lineup.Lineup;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SaveLineupResponseDto {

    private Long lineupId;
    private Long matchId;
    private int quarter;
    private String formation;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "Asia/Seoul")
    private LocalDateTime updatedAt;

    public static SaveLineupResponseDto from(Lineup lineup) {
        return SaveLineupResponseDto.builder()
                .lineupId(lineup.getId())
                .matchId(lineup.getMatch().getId())
                .quarter(lineup.getQuarter())
                .formation(lineup.getFormation())
                .updatedAt(lineup.getUpdatedAt())
                .build();
    }
}
