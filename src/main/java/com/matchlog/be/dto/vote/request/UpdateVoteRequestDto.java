package com.matchlog.be.dto.vote.request;

import com.matchlog.be.constant.vote.VoteStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UpdateVoteRequestDto {

    private VoteStatus status;
}
