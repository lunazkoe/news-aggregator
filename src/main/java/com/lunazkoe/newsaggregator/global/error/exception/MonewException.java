package com.lunazkoe.newsaggregator.global.error.exception;

import com.lunazkoe.newsaggregator.global.error.ErrorCode;
import lombok.Getter;

import java.util.Map;

@Getter
public abstract class MonewException extends RuntimeException{

    private final ErrorCode errorCode;
    private final Map<String, Object> details;

    protected MonewException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
        this.details = Map.of();
    }

    protected MonewException(ErrorCode errorCode, Map<String, Object> details) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
        this.details = details;
    }
}
