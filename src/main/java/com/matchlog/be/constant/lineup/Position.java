package com.matchlog.be.constant.lineup;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Position {

    // Defender
    CB("센터백", PositionCategory.DF),
    LB("레프트백", PositionCategory.DF),
    RB("라이트백", PositionCategory.DF),
    WB("윙백", PositionCategory.DF),

    // Midfielder
    CDM("수비형 미드필더", PositionCategory.MF),
    CM("중앙 미드필더", PositionCategory.MF),
    LM("레프트 미드필더", PositionCategory.MF),
    RM("라이트 미드필더", PositionCategory.MF),
    CAM("중앙 공격형 미드필더", PositionCategory.MF),
    AM("공격형 미드필더", PositionCategory.MF),

    // Forward
    LW("레프트 윙어", PositionCategory.FW),
    RW("라이트 윙어", PositionCategory.FW),
    ST("스트라이커", PositionCategory.FW),
    CF("센터 포워드", PositionCategory.FW);

    private final String description;
    private final PositionCategory category;

    public enum PositionCategory {
        DF,
        MF,
        FW
    }
}
