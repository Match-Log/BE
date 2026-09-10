package com.matchlog.be.constant.lineup;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Position {

    // Goalkeeper
    GK("골키퍼", PositionCategory.GK),

    // Defender (센터백, 좌/우 백, 윙백, 스위퍼)
    CB("중앙 센터백", PositionCategory.DF),
    LCB("레프트 센터백", PositionCategory.DF),
    RCB("라이트 센터백", PositionCategory.DF),
    LB("레프트백", PositionCategory.DF),
    RB("라이트백", PositionCategory.DF),
    LWB("레프트 윙백", PositionCategory.DF),
    RWB("라이트 윙백", PositionCategory.DF),
    SW("스위퍼", PositionCategory.DF),

    // Midfielder (수비형, 중앙, 공격형, 측면 미드필더)
    CDM("중앙 수비형 미드필더", PositionCategory.MF),
    LDM("레프트 수비형 미드필더", PositionCategory.MF),
    RDM("라이트 수비형 미드필더", PositionCategory.MF),
    CM("중앙 미드필더", PositionCategory.MF),
    LCM("레프트 중앙 미드필더", PositionCategory.MF),
    RCM("라이트 중앙 미드필더", PositionCategory.MF),
    CAM("중앙 공격형 미드필더", PositionCategory.MF),
    LAM("레프트 공격형 미드필더", PositionCategory.MF),
    RAM("라이트 공격형 미드필더", PositionCategory.MF),
    LM("레프트 미드필더", PositionCategory.MF),
    RM("라이트 미드필더", PositionCategory.MF),

    // Forward (스트라이커, 포워드, 윙어)
    ST("중앙 스트라이커", PositionCategory.FW),
    LS("레프트 스트라이커", PositionCategory.FW),
    RS("라이트 스트라이커", PositionCategory.FW),
    CF("센터 포워드", PositionCategory.FW),
    LF("레프트 포워드", PositionCategory.FW),
    RF("라이트 포워드", PositionCategory.FW),
    LW("레프트 윙어", PositionCategory.FW),
    RW("라이트 윙어", PositionCategory.FW);

    private final String description;
    private final PositionCategory category;

    public enum PositionCategory {
        GK,
        DF,
        MF,
        FW
    }
}
