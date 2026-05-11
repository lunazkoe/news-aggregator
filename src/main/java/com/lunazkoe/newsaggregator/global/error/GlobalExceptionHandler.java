package com.lunazkoe.newsaggregator.global.error;

import com.lunazkoe.newsaggregator.global.error.exception.MonewException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // 커스텀 비즈니스 예외 처리
    @ExceptionHandler(MonewException.class)
    public ResponseEntity<ErrorResponse> handlerMonewException(MonewException e) {
        ErrorCode errorCode = e.getErrorCode();
        log.warn("[MonewException] Code: {}, Message: {}", errorCode.getCode(), errorCode.getMessage());

        return ResponseEntity.status(errorCode.getHttpStatus())
                .body(ErrorResponse.of(errorCode));
    }

    // 입력값 검증(Bean Validation) 실패 예외 처리
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException e) {
        // 여러 에러 중 첫 번째 에러 메시지만 추출하여 반환
        String errorMessage = e.getBindingResult().getAllErrors().get(0).getDefaultMessage();
        log.warn("[ValidationException] Message: {}", errorMessage);

        return ResponseEntity.status(GlobalErrorCode.BAD_REQUEST.getHttpStatus())
                .body(ErrorResponse.of(GlobalErrorCode.BAD_REQUEST.getCode(), errorMessage));
    }

    // 예상치 못한 서버 내부 오류 처리
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception e) {
        log.error("[UnhandledException] Message: {}", e.getMessage(), e);

        return ResponseEntity.status(GlobalErrorCode.INTERNAL_SERVER_ERROR.getHttpStatus())
                .body(ErrorResponse.of(GlobalErrorCode.INTERNAL_SERVER_ERROR));
    }
}

// e는 언제 찍어야할까
// - Exception.class는 e를 찍어주는 것이 디버깅에 좋음
// - CustomException은 에러가 아닌 예외(에상 가능한 흐름)이기 때문에 e를 호출하는 것이 비용이 될 수 있음