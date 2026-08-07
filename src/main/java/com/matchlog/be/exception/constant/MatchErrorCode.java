package com.matchlog.be.exception.constant;

import com.matchlog.be.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@RequiredArgsConstructor
public enum MatchErrorCode implements ErrorCode {

    MATCH_NOT_FOUND(HttpStatus.NOT_FOUND, "MATCH_NOT_FOUND", "존재하지 않는 경기입니다."),
    ALREADY_VOTED(HttpStatus.CONFLICT, "ALREADY_VOTED", "이미 투표한 경기입니다."),
    VOTE_NOT_FOUND(HttpStatus.NOT_FOUND, "VOTE_NOT_FOUND", "투표 기록이 없습니다. 먼저 투표해주세요."),
    INVALID_MATCH_DATE(HttpStatus.BAD_REQUEST, "INVALID_MATCH_DATE", "경기 날짜는 현재 이후여야 합니다."),
    MATCH_ALREADY_EXISTS(HttpStatus.CONFLICT, "MATCH_ALREADY_EXISTS", "해당 날짜에 이미 경기가 존재합니다."),
    MATCH_ALREADY_FINISHED(HttpStatus.CONFLICT, "MATCH_ALREADY_FINISHED", "종료된 경기는 수정할 수 없습니다."),
    MATCH_NOT_FINISHED(HttpStatus.CONFLICT, "MATCH_NOT_FINISHED", "종료된 경기에만 스탯을 입력할 수 있습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;

    @Override public HttpStatus getStatus() { return status; }
    @Override public String getCode() { return code; }
    @Override public String getMessage() { return message; }
}
