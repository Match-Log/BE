package com.matchlog.be.exception.constant;

import com.matchlog.be.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@RequiredArgsConstructor
public enum StatErrorCode implements ErrorCode {
    INVALID_STAT_FOR_POSITION(
            HttpStatus.BAD_REQUEST, "INVALID_STAT_FOR_POSITION", "해당 포지션에는 입력할 수 없는 스탯입니다.");

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
