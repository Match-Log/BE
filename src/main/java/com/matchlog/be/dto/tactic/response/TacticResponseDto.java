package com.matchlog.be.dto.tactic.response;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.matchlog.be.domain.tactic.PersonalTactic;
import com.matchlog.be.domain.tactic.TeamTactic;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TacticResponseDto {

    private Long tacticId;
    private Long matchId;
    private String scope;
    private Long playerId;
    private String content;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "Asia/Seoul")
    private LocalDateTime updatedAt;

    public static TacticResponseDto from(TeamTactic tactic) {
        return TacticResponseDto.builder()
                .tacticId(tactic.getId())
                .matchId(tactic.getMatch().getId())
                .scope("team")
                .playerId(null)
                .content(tactic.getContent())
                .updatedAt(tactic.getUpdatedAt())
                .build();
    }

    public static TacticResponseDto from(PersonalTactic tactic) {
        return TacticResponseDto.builder()
                .tacticId(tactic.getId())
                .matchId(tactic.getMatch().getId())
                .scope("player")
                .playerId(tactic.getPlayer().getId())
                .content(tactic.getContent())
                .updatedAt(tactic.getUpdatedAt())
                .build();
    }
}
