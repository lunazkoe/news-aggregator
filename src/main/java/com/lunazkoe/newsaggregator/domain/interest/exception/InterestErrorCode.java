package com.lunazkoe.newsaggregator.domain.interest.exception;

import com.lunazkoe.newsaggregator.global.error.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum InterestErrorCode implements ErrorCode {

    EXIST_SIMILARITY_NAME(HttpStatus.CONFLICT, "I001", "이미 유사한 관심사 이름이 존재합니다."),
    INTEREST_NOT_FOUND(HttpStatus.NOT_FOUND, "I002", "관심사를 찾을 수 없습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
