package com.matchlog.be.exception.constant;

import com.matchlog.be.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@RequiredArgsConstructor
public enum TeamErrorCode implements ErrorCode {
    TEAM_NOT_FOUND(HttpStatus.NOT_FOUND, "TEAM_NOT_FOUND", "존재하지 않는 팀입니다."),
    INVITE_CODE_NOT_FOUND(HttpStatus.NOT_FOUND, "INVITE_CODE_NOT_FOUND", "유효하지 않은 초대 코드입니다."),
    ALREADY_JOINED(HttpStatus.CONFLICT, "ALREADY_JOINED", "이미 가입된 팀입니다."),
    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "MEMBER_NOT_FOUND", "해당 팀에 존재하지 않는 멤버입니다."),
    TEAM_LIMIT_EXCEEDED(HttpStatus.CONFLICT, "TEAM_LIMIT_EXCEEDED", "최대 2개 팀까지 소속될 수 있습니다."),
    LAST_MANAGER_CANNOT_LEAVE(
            HttpStatus.CONFLICT, "LAST_MANAGER_CANNOT_LEAVE", "MANAGER가 한 명입니다. 역할을 이전한 후 변경하세요."),
    LAST_MANAGER_CANNOT_BE_REMOVED(
            HttpStatus.CONFLICT, "LAST_MANAGER_CANNOT_BE_REMOVED", "팀의 마지막 MANAGER는 제외할 수 없습니다."),
    CAPTAIN_ALREADY_ASSIGNED(HttpStatus.CONFLICT, "CAPTAIN_ALREADY_ASSIGNED", "이미 주장이 지정되어 있습니다."),
    PK_TAKER_ALREADY_ASSIGNED(
            HttpStatus.CONFLICT, "PK_TAKER_ALREADY_ASSIGNED", "이미 PK 키커가 지정되어 있습니다.");

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
