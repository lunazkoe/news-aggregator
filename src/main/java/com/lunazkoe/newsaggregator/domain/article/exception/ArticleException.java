package com.lunazkoe.newsaggregator.domain.article.exception;

import com.lunazkoe.newsaggregator.global.error.ErrorCode;
import com.lunazkoe.newsaggregator.global.error.exception.MonewException;

import java.util.Map;

public class ArticleException extends MonewException {
    public ArticleException(ErrorCode errorCode) {
        super(errorCode);
    }

    public ArticleException(ErrorCode errorCode, Map<String, Object> details) {
        super(errorCode, details);
    }
}
