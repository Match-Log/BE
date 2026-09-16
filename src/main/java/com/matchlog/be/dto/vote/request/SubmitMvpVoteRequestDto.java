package com.matchlog.be.dto.vote.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SubmitMvpVoteRequestDto {

    private Long votedPlayerId;
}