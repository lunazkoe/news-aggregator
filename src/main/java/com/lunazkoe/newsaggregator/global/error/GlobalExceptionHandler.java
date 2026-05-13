package com.lunazkoe.newsaggregator.global.error;

import com.lunazkoe.newsaggregator.global.error.exception.MonewException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // 커스텀 비즈니스 예외 처리
    @ExceptionHandler(MonewException.class)
    public ResponseEntity<ErrorResponse> handlerMonewException(MonewException e) {
        ErrorCode errorCode = e.getErrorCode();
        log.warn("[MonewException] Code: {}, Message: {}", errorCode.getCode(), errorCode.getMessage());

        return ResponseEntity.status(errorCode.getHttpStatus())
                .body(ErrorResponse.of(errorCode, e));
    }

    // 입력값 검증(Bean Validation) 실패 예외 처리(@RequestBody / @ModelAttribute)
    @ExceptionHandler(BindException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(BindException e) {
        String errorMessage = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));
        log.warn("[ValidationException] Message: {}", errorMessage);

        return ResponseEntity.status(GlobalErrorCode.BAD_REQUEST.getHttpStatus())
                .body(ErrorResponse.of(GlobalErrorCode.BAD_REQUEST, errorMessage, e));
    }

    // 예상치 못한 서버 내부 오류 처리
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception e) {
        log.error("[UnhandledException] Message: {}", e.getMessage(), e);

        return ResponseEntity.status(GlobalErrorCode.INTERNAL_SERVER_ERROR.getHttpStatus())
                .body(ErrorResponse.of(GlobalErrorCode.INTERNAL_SERVER_ERROR, e));
    }
}

// e는 언제 찍어야할까
// - Exception.class는 e를 찍어주는 것이 디버깅에 좋음
// - CustomException은 에러가 아닌 예외(에상 가능한 흐름)이기 때문에 e를 호출하는 것이 비용이 될 수 있음