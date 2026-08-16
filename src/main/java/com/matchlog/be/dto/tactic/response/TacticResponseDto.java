package com.matchlog.be.dto.tactic.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.matchlog.be.constant.tactic.CrossType;
import com.matchlog.be.constant.tactic.DefensiveLineHeight;
import com.matchlog.be.constant.tactic.DefensiveSpacing;
import com.matchlog.be.constant.tactic.FlankDefense;
import com.matchlog.be.constant.tactic.PossessionStrategy;
import com.matchlog.be.constant.tactic.PressingTrigger;
import com.matchlog.be.constant.tactic.SetPieceAttack;
import com.matchlog.be.constant.tactic.SetPieceDefense;
import com.matchlog.be.domain.tactic.PersonalTactic;
import com.matchlog.be.domain.tactic.TeamTactic;
import java.time.LocalDateTime;
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

    private DefensiveLineHeight defensiveLineHeight;
    private DefensiveSpacing defensiveSpacing;
    private SetPieceDefense setPieceDefense;
    private FlankDefense flankDefense;
    private Boolean offsideTrap;
    private CrossType crossType;
    private SetPieceAttack setPieceAttack;
    private PossessionStrategy possessionStrategy;
    private PressingTrigger pressingTrigger;
    private Integer pressingIntensity;

    @JsonFormat(
            shape = JsonFormat.Shape.STRING,
            pattern = "yyyy-MM-dd'T'HH:mm:ss",
            timezone = "Asia/Seoul")
    private LocalDateTime updatedAt;

    public static TacticResponseDto from(TeamTactic tactic) {
        return TacticResponseDto.builder()
                .tacticId(tactic.getId())
                .matchId(tactic.getMatch().getId())
                .scope("team")
                .playerId(null)
                .content(tactic.getContent())
                .defensiveLineHeight(tactic.getDefensiveLineHeight())
                .defensiveSpacing(tactic.getDefensiveSpacing())
                .setPieceDefense(tactic.getSetPieceDefense())
                .flankDefense(tactic.getFlankDefense())
                .offsideTrap(tactic.getOffsideTrap())
                .crossType(tactic.getCrossType())
                .setPieceAttack(tactic.getSetPieceAttack())
                .possessionStrategy(tactic.getPossessionStrategy())
                .pressingTrigger(tactic.getPressingTrigger())
                .pressingIntensity(tactic.getPressingIntensity())
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
