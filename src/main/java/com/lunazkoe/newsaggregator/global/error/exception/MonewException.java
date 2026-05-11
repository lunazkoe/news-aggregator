package com.lunazkoe.newsaggregator.global.error.exception;

import com.lunazkoe.newsaggregator.global.error.ErrorCode;
import lombok.Getter;

@Getter
public abstract class MonewException extends RuntimeException{

    private final ErrorCode errorCode;

    protected MonewException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
}
