package com.lunazkoe.newsaggregator.global.error;

import com.lunazkoe.newsaggregator.global.error.exception.MonewException;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.time.Instant;
import java.util.Map;

public record ErrorResponse(
        Instant timestamp,
        String code,
        String message,
        Map<String, Object> details,
        String exceptionType,
        Integer status
) {
    public static ErrorResponse of(ErrorCode errorCode, MonewException e) {
        return new ErrorResponse(
                Instant.now(),
                errorCode.getCode(),
                errorCode.getMessage(),
                e.getDetails(),
                e.getClass().getSimpleName(),
                errorCode.getHttpStatus().value()
        );
    }

    public static ErrorResponse of(ErrorCode errorCode, String message, MethodArgumentNotValidException e) {
        return new ErrorResponse(
                Instant.now(),
                errorCode.getCode(),
                message,
                Map.of(),
                e.getClass().getSimpleName(),
                errorCode.getHttpStatus().value()
        );
    }

    public static ErrorResponse of(ErrorCode errorCode, Exception e) {
        return new ErrorResponse(
                Instant.now(),
                errorCode.getCode(),
                errorCode.getMessage(),
                Map.of(),
                e.getClass().getSimpleName(),
                errorCode.getHttpStatus().value()
        );
    }
}


//{
//    "timestamp": "2026-05-12T12:31:32.833962518Z",
//    "code": "INTEREST_NAME_DUPLICATION",
//    "message": "유사한 이름의 관심사가 이미 존재합니다.",
//    "details": {
//        "name": "lunz"
//    },
//    "exceptionType": "InterestException",
//    "status": 409
//}