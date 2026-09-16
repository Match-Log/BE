package com.matchlog.be.dto.stat.response;

import com.matchlog.be.constant.stat.StatPeriod;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PlayerStatSummaryResponseDto {

    private Long playerId;
    private StatPeriod period;
    private int year;
    private Integer month;
    private long matchCount;
    private long goals;
    private long assists;
    private Long saves;
    private Long cleanSheetCount;
    private long mvpCount;
    private Double averageRating;
}