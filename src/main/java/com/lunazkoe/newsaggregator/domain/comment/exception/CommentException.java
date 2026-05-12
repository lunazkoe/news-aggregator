package com.lunazkoe.newsaggregator.domain.comment.exception;

import com.lunazkoe.newsaggregator.global.error.ErrorCode;
import com.lunazkoe.newsaggregator.global.error.exception.MonewException;

import java.util.Map;

public class CommentException extends MonewException {
    public CommentException(ErrorCode errorCode) {
        super(errorCode);
    }

    public CommentException(ErrorCode errorCode, Map<String, Object> details) {
        super(errorCode, details);
    }
}
