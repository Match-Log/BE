package com.matchlog.be.dto.tactic.request;

import com.matchlog.be.constant.tactic.CrossType;
import com.matchlog.be.constant.tactic.DefensiveLineHeight;
import com.matchlog.be.constant.tactic.DefensiveSpacing;
import com.matchlog.be.constant.tactic.FlankDefense;
import com.matchlog.be.constant.tactic.PossessionStrategy;
import com.matchlog.be.constant.tactic.PressingTrigger;
import com.matchlog.be.constant.tactic.SetPieceAttack;
import com.matchlog.be.constant.tactic.SetPieceDefense;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SaveTacticRequestDto {

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
}
