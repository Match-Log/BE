package com.matchlog.be.exception.constant;

import com.matchlog.be.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@RequiredArgsConstructor
public enum PlayerErrorCode implements ErrorCode {
    PLAYER_NOT_FOUND(HttpStatus.NOT_FOUND, "PLAYER_NOT_FOUND", "존재하지 않는 선수입니다."),
    PLAYER_ALREADY_EXISTS(HttpStatus.CONFLICT, "PLAYER_ALREADY_EXISTS", "이미 선수 등록이 완료된 계정입니다.");

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
