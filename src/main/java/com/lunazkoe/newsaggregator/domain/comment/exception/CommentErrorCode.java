package com.lunazkoe.newsaggregator.domain.comment.exception;

import com.lunazkoe.newsaggregator.global.error.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum CommentErrorCode implements ErrorCode {

    COMMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "COMMNET001", "댓글을 찾을 수 없습니다."),
    FORBIDDEN(HttpStatus.FORBIDDEN, "COMMENT001", "해당 댓글을 수정/삭제할 권한이 없습니다."),
    ALREADY_LIKED(HttpStatus.CONFLICT, "ALREADY_LIKED", "이미 좋아요를 눌렀습니다."),
    LIKE_NOT_FOUND(HttpStatus.NOT_FOUND, "LIKE_NOT_FOUND", "좋아요를 누른 적이 없습니다.");

    private final HttpStatus httpStatus;
    private final String Code;
    private final String message;
}
