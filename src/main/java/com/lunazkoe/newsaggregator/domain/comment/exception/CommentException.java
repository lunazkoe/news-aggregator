package com.lunazkoe.newsaggregator.domain.comment.exception;

import com.lunazkoe.newsaggregator.global.error.ErrorCode;
import com.lunazkoe.newsaggregator.global.error.exception.MonewException;

public class CommentException extends MonewException {
    public CommentException(ErrorCode errorCode) {
        super(errorCode);
    }
}
