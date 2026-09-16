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
public class MvpVoteStatusResponseDto {

    private Long matchId;
    private int totalVotes;
    private List<MvpVoteResultItemResponseDto> results;
    private List<Long> winners;
    private Long myVote;
}