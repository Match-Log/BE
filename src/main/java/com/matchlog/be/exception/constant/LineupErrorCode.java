package com.matchlog.be.exception.constant;

import com.matchlog.be.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@RequiredArgsConstructor
public enum LineupErrorCode implements ErrorCode {
    DUPLICATE_PLAYER_IN_LINEUP(
            HttpStatus.BAD_REQUEST, "DUPLICATE_PLAYER_IN_LINEUP", "동일 선수는 한 번만 배치할 수 있습니다."),
    DUPLICATE_GUEST_IN_LINEUP(
            HttpStatus.BAD_REQUEST, "DUPLICATE_GUEST_IN_LINEUP", "동일 용병은 한 번만 배치할 수 있습니다."),
    INVALID_SPOT_SUBJECT(
            HttpStatus.BAD_REQUEST,
            "INVALID_SPOT_SUBJECT",
            "각 스팟에는 팀 선수(playerId) 또는 용병(guestId) 중 하나만 지정해야 합니다."),
    GUEST_NOT_FOUND(HttpStatus.NOT_FOUND, "GUEST_NOT_FOUND", "존재하지 않는 용병입니다."),
    GUEST_NOT_IN_MATCH(HttpStatus.BAD_REQUEST, "GUEST_NOT_IN_MATCH", "해당 경기에 등록된 용병이 아닙니다.");

    // DUPLICATE_POSITION_IN_LINEUP(
    //         HttpStatus.BAD_REQUEST, "DUPLICATE_POSITION_IN_LINEUP", "동일 포지션에 두 명을 배치할 수 없습니다.");
    // 동일 포지션 관리는 너무 과설계인 것 같아서 일단 뒤로 미룸
    // 각각의 포지션 EX) CB / CB  ->  LCB / RCB 이렇게 프론트측에서 나눌 거면 모르겠는 아직은 애매한 것 같아서 보류

    private final HttpStatus status;
    private final String code;
    private final String message;

    @Override
    public HttpStatus getStatus() {
        return status;
    }

    @Override
    public String getCode() {
        return code;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
