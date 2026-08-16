package com.matchlog.be.dto.match.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.matchlog.be.domain.match.Match;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UpdateMatchResponseDto {

    private Long matchId;
    private String opponent;
    private boolean isFinished;
    private Integer scoreHome;
    private Integer scoreAway;

    @JsonFormat(
            shape = JsonFormat.Shape.STRING,
            pattern = "yyyy-MM-dd'T'HH:mm:ss",
            timezone = "Asia/Seoul")
    private LocalDateTime updatedAt;

    public static UpdateMatchResponseDto from(Match match) {
        return UpdateMatchResponseDto.builder()
                .matchId(match.getId())
                .opponent(match.getOpponent())
                .isFinished(match.isFinished())
                .scoreHome(match.getScoreHome())
                .scoreAway(match.getScoreAway())
                .updatedAt(match.getUpdatedAt())
                .build();
    }
}
