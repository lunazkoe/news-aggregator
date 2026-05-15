package com.lunazkoe.newsaggregator.domain.activity.exception;

import com.lunazkoe.newsaggregator.global.error.ErrorCode;
import com.lunazkoe.newsaggregator.global.error.exception.MonewException;

import java.util.Map;

public class UserActivityException extends MonewException {
    public UserActivityException(ErrorCode errorCode) {
        super(errorCode);
    }

    public UserActivityException(ErrorCode errorCode, Map<String, Object> details) {
        super(errorCode, details);
    }
}
