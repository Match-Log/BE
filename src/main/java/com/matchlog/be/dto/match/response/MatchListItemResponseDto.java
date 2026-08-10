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
public class MatchListItemResponseDto {

    private Long matchId;
    private String opponent;

    @JsonFormat(
            shape = JsonFormat.Shape.STRING,
            pattern = "yyyy-MM-dd'T'HH:mm:ss",
            timezone = "Asia/Seoul")
    private LocalDateTime matchDate;

    private String location;
    private HomeAway homeAway;
    private MatchType matchType;
    private Integer scoreHome;
    private Integer scoreAway;
    private boolean isFinished;

    public static MatchListItemResponseDto from(Match match) {
        return MatchListItemResponseDto.builder()
                .matchId(match.getId())
                .opponent(match.getOpponent())
                .matchDate(match.getMatchDate())
                .location(match.getLocation())
                .homeAway(match.getHomeAway())
                .matchType(match.getMatchType())
                .scoreHome(match.getScoreHome())
                .scoreAway(match.getScoreAway())
                .isFinished(match.isFinished())
                .build();
    }
}
