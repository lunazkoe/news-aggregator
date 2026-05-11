package com.lunazkoe.newsaggregator.domain.user.exception;

import com.lunazkoe.newsaggregator.global.error.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum UserErrorCode implements ErrorCode {

    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "U001", "사용자 정보를 찾을 수 없습니다."),
    EMAIL_ALREADY_EXISTS(HttpStatus.CONFLICT, "U002", "이미 존재하는 이메일입니다."),
    INVALID_PASSWORD(HttpStatus.UNAUTHORIZED, "U003", "비밀번호가 일치하지 않습니다."),
    UNAUTHORIZED_ACTION(HttpStatus.FORBIDDEN, "U004", "해당 작업을 수행할 권한이 없습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
