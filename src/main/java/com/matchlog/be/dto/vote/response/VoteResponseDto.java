package com.matchlog.be.dto.vote.response;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.matchlog.be.constant.vote.VoteStatus;
import com.matchlog.be.domain.vote.Vote;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class VoteResponseDto {

    private Long matchId;
    private Long playerId;
    private VoteStatus status;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "Asia/Seoul")
    private LocalDateTime votedAt;

    public static VoteResponseDto from(Vote vote) {
        return VoteResponseDto.builder()
                .matchId(vote.getMatch().getId())
                .playerId(vote.getPlayer().getId())
                .status(vote.getStatus())
                .votedAt(vote.getVotedAt())
                .build();
    }
}
