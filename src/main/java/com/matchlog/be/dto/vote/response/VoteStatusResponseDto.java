package com.matchlog.be.dto.vote.response;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class VoteStatusResponseDto {

    private Long matchId;
    private int total;
    private int attend;
    private int pending;
    private int absent;
    private List<VoteItemResponseDto> votes;
}
