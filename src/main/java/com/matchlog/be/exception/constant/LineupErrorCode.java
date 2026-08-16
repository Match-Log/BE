package com.matchlog.be.exception.constant;

import com.matchlog.be.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@RequiredArgsConstructor
public enum LineupErrorCode implements ErrorCode {
    DUPLICATE_PLAYER_IN_LINEUP(
            HttpStatus.BAD_REQUEST, "DUPLICATE_PLAYER_IN_LINEUP", "동일 선수는 한 번만 배치할 수 있습니다."),
    DUPLICATE_POSITION_IN_LINEUP(
            HttpStatus.BAD_REQUEST, "DUPLICATE_POSITION_IN_LINEUP", "동일 포지션에 두 명을 배치할 수 없습니다.");

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
