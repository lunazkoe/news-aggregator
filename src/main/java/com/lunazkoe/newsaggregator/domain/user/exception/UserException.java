package com.lunazkoe.newsaggregator.domain.user.exception;

import com.lunazkoe.newsaggregator.global.error.ErrorCode;
import com.lunazkoe.newsaggregator.global.error.exception.MonewException;

public class UserException extends MonewException {
    public UserException(ErrorCode errorCode) {
        super(errorCode);
    }
}
