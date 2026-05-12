package com.lunazkoe.newsaggregator.domain.comment.controller;

import com.lunazkoe.newsaggregator.domain.comment.dto.request.CommentRegisterRequest;
import com.lunazkoe.newsaggregator.domain.comment.dto.request.CommentUpdateRequest;
import com.lunazkoe.newsaggregator.domain.comment.dto.response.CommentDto;
import com.lunazkoe.newsaggregator.domain.comment.service.CommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

import static com.lunazkoe.newsaggregator.global.filter.MDCLoggingFilter.HEADER_USER_ID;

@Slf4j
@RestController
@RequestMapping("/api/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CommentDto commentRegister(@Valid @RequestBody CommentRegisterRequest request) {
        log.info("Request to register comment for article ID: {} by user ID: {}", request.articleId(), request.userId());
        CommentDto response = commentService.register(request);
        return response;
    }

    @PatchMapping("/{commentId}")
    @ResponseStatus(HttpStatus.OK)
    public CommentDto commentUpdate(
            @PathVariable UUID commentId,
            @RequestHeader(HEADER_USER_ID) UUID requestUserId,
            @Valid @RequestBody CommentUpdateRequest request
    ) {
        log.info("Request to update comment ID: {} by user ID: {}", commentId, requestUserId);
        CommentDto response = commentService.update(commentId, requestUserId, request);
        return response;
    }

    @DeleteMapping("/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void softDeleteComment(
            @PathVariable UUID commentId,
            @RequestHeader(HEADER_USER_ID) UUID requestUserId
    ) {
        log.info("Request to soft delete comment ID: {} by user ID: {}", commentId, requestUserId);
        commentService.softDelete(commentId, requestUserId);
    }

    @DeleteMapping("/{commentId}/hard")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void hardDeleteComment(
            @PathVariable UUID commentId
    ) {
        log.info("Request to hard delete comment ID: {}", commentId);
        commentService.hardDelete(commentId);
    }

    // - 댓글 목록 조회
    // - 댓글 좋아요
    // - 댓글 좋아요 취소
}
