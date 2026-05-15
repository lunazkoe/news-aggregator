package com.lunazkoe.newsaggregator.domain.activity.exception;

import com.lunazkoe.newsaggregator.global.error.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum UserActivityErrorCode implements ErrorCode {

    USER_ACTIVITY_NOT_FOUND(HttpStatus.NOT_FOUND, "USER_ACTIVITY_NOT_FOUND", "사용자 활동 내역을 찾을 수 없습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
