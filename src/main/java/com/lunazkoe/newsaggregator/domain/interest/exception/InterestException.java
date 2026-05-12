package com.lunazkoe.newsaggregator.domain.interest.exception;

import com.lunazkoe.newsaggregator.global.error.ErrorCode;
import com.lunazkoe.newsaggregator.global.error.exception.MonewException;

public class InterestException extends MonewException {
    public InterestException(ErrorCode errorCode) {
        super(errorCode);
    }
}
