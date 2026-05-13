package com.lunazkoe.newsaggregator.domain.article.exception;

import com.lunazkoe.newsaggregator.global.error.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ArticleErrorCode implements ErrorCode {

    ARTICLE_NOT_FOUND(HttpStatus.NOT_FOUND, "ARTICLE_NOT_FOUND", "뉴스 기사가 없습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
