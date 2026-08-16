package com.matchlog.be.dto.match.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.matchlog.be.constant.match.HomeAway;
import com.matchlog.be.constant.match.MatchType;
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
public class CreateMatchResponseDto {

    private Long matchId;
    private Long teamId;
    private String opponent;

    @JsonFormat(
            shape = JsonFormat.Shape.STRING,
            pattern = "yyyy-MM-dd'T'HH:mm:ss",
            timezone = "Asia/Seoul")
    private LocalDateTime matchDate;

    private String location;
    private HomeAway homeAway;
    private MatchType matchType;
    private boolean isFinished;

    @JsonFormat(
            shape = JsonFormat.Shape.STRING,
            pattern = "yyyy-MM-dd'T'HH:mm:ss",
            timezone = "Asia/Seoul")
    private LocalDateTime createdAt;

    private Long boardId;
    private String boardTitle;

    public static CreateMatchResponseDto from(Match match, Long boardId, String boardTitle) {
        return CreateMatchResponseDto.builder()
                .matchId(match.getId())
                .teamId(match.getTeam().getId())
                .opponent(match.getOpponent())
                .matchDate(match.getMatchDate())
                .location(match.getLocation())
                .homeAway(match.getHomeAway())
                .matchType(match.getMatchType())
                .isFinished(match.isFinished())
                .createdAt(match.getCreatedAt())
                .boardId(boardId)
                .boardTitle(boardTitle)
                .build();
    }
}
