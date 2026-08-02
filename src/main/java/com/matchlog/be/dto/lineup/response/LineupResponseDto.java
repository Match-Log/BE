package com.matchlog.be.dto.lineup.response;

import java.util.List;

import com.matchlog.be.constant.match.MatchType;
import com.matchlog.be.domain.lineup.Lineup;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LineupResponseDto {

    private Long lineupId;
    private Long matchId;
    private int quarter;
    private String formation;
    private MatchType matchType;
    private List<LineupSpotResponseDto> spots;

    public static LineupResponseDto of(Lineup lineup, List<LineupSpotResponseDto> spots) {
        return LineupResponseDto.builder()
                .lineupId(lineup.getId())
                .matchId(lineup.getMatch().getId())
                .quarter(lineup.getQuarter())
                .formation(lineup.getFormation())
                .matchType(lineup.getMatch().getMatchType())
                .spots(spots)
                .build();
    }
}
