package com.matchlog.be.dto.vote.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.matchlog.be.constant.vote.VoteStatus;
import com.matchlog.be.domain.vote.Vote;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class VoteItemResponseDto {

    private Long playerId;
    private String name;
    private String profileImage;
    private VoteStatus status;

    @JsonFormat(
            shape = JsonFormat.Shape.STRING,
            pattern = "yyyy-MM-dd'T'HH:mm:ss",
            timezone = "Asia/Seoul")
    private LocalDateTime votedAt;

    public static VoteItemResponseDto from(Vote vote) {
        return VoteItemResponseDto.builder()
                .playerId(vote.getPlayer().getId())
                .name(vote.getPlayer().getUser().getName())
                .profileImage(vote.getPlayer().getUser().getProfileImage())
                .status(vote.getStatus())
                .votedAt(vote.getVotedAt())
                .build();
    }
}
