package com.matchlog.be.exception.constant;

import com.matchlog.be.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@RequiredArgsConstructor
public enum DocumentErrorCode implements ErrorCode {
    BOARD_NOT_FOUND(HttpStatus.NOT_FOUND, "BOARD_NOT_FOUND", "존재하지 않는 게시글입니다."),
    VOTE_DOCUMENT_CANNOT_BE_MODIFIED(
            HttpStatus.CONFLICT, "VOTE_DOCUMENT_CANNOT_BE_MODIFIED", "투표 게시글은 수정할 수 없습니다."),
    VOTE_DOCUMENT_CANNOT_BE_DELETED(
            HttpStatus.CONFLICT,
            "VOTE_DOCUMENT_CANNOT_BE_DELETED",
            "투표 게시글은 삭제할 수 없습니다. 경기를 삭제하세요.");

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
