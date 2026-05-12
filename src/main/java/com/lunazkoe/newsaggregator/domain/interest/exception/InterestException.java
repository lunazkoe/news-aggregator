package com.lunazkoe.newsaggregator.domain.interest.exception;

import com.lunazkoe.newsaggregator.global.error.ErrorCode;
import com.lunazkoe.newsaggregator.global.error.exception.MonewException;

import java.util.Map;

public class InterestException extends MonewException {
    public InterestException(ErrorCode errorCode) {
        super(errorCode);
    }

    public InterestException(ErrorCode errorCode, Map<String, Object> details) {
        super(errorCode, details);
    }
}
