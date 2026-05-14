package com.lunazkoe.newsaggregator.domain.user.exception;

import com.lunazkoe.newsaggregator.global.error.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum UserErrorCode implements ErrorCode {

    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "USER_NOT_FOUND", "사용자 정보를 찾을 수 없습니다."),
    EMAIL_ALREADY_EXISTS(HttpStatus.CONFLICT, "EMAIL_ALREADY_EXISTS", "이미 존재하는 이메일입니다."),
    EMAIL_OR_PASSWORD_INVALID(HttpStatus.UNAUTHORIZED, "EMAIL_OR_PASSWORD_INVALID", "이메일 또는 비밀번호가 일치하지 않습니다."),
    UNAUTHORIZED_ACTION(HttpStatus.FORBIDDEN, "UNAUTHORIZED_ACTION", "해당 작업을 수행할 권한이 없습니다.");


    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
