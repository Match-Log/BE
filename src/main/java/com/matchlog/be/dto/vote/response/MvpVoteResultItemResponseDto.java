package com.matchlog.be.dto.vote.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MvpVoteResultItemResponseDto {

    private Long playerId;
    private String name;
    private int voteCount;
}
