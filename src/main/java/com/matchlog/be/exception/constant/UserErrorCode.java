package com.matchlog.be.exception.constant;

import com.matchlog.be.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@RequiredArgsConstructor
public enum UserErrorCode implements ErrorCode {
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "USER_NOT_FOUND", "존재하지 않는 유저입니다."),
    MANAGER_TEAM_EXISTS(
            HttpStatus.CONFLICT,
            "MANAGER_TEAM_EXISTS",
            "MANAGER로 등록된 팀이 있습니다. 팀을 삭제하거나 역할을 이전 후 탈퇴하세요.");

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
