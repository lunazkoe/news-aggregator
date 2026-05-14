package com.lunazkoe.newsaggregator.domain.comment.controller;

import com.lunazkoe.newsaggregator.domain.comment.dto.request.CommentRegisterRequest;
import com.lunazkoe.newsaggregator.domain.comment.dto.request.CommentUpdateRequest;
import com.lunazkoe.newsaggregator.domain.comment.dto.response.CommentDto;
import com.lunazkoe.newsaggregator.domain.comment.dto.response.CommentLikeDto;
import com.lunazkoe.newsaggregator.domain.comment.service.CommentService;
import com.lunazkoe.newsaggregator.global.filter.MDCLoggingFilter;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

import static com.lunazkoe.newsaggregator.global.filter.MDCLoggingFilter.*;

@Slf4j
@RestController
@RequestMapping("/api/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "댓글 등록", description = "새로운 댓글을 등록합니다.")
    public CommentDto commentRegister(@Valid @RequestBody CommentRegisterRequest request) {
        log.info("Request to register comment for article ID: {} by user ID: {}", request.articleId(), request.userId());
        CommentDto response = commentService.register(request);
        return response;
    }

    @PatchMapping("/{commentId}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "댓글 정보 수정", description = "댓글의 내용을 수정합니다.")
    public CommentDto commentUpdate(
            @PathVariable UUID commentId,
            @Valid @RequestBody CommentUpdateRequest request
    ) {
        log.info("Request to update comment ID: {}", commentId);
        CommentDto response = commentService.update(commentId, request);
        return response;
    }

    @DeleteMapping("/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "댓글 논리 삭제", description = "댓글을 논리적으로 삭제합니다.")
    public void softDeleteComment(
            @PathVariable UUID commentId
    ) {
        log.info("Request to soft delete comment ID: {}", commentId);
        commentService.softDelete(commentId);
    }

    @DeleteMapping("/{commentId}/hard")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "댓글 물리 삭제", description = "댓글을 물리적으로 삭제합니다.")
    public void hardDeleteComment(
            @PathVariable UUID commentId
    ) {
        log.info("Request to hard delete comment ID: {}", commentId);
        commentService.hardDelete(commentId);
    }

    // - 댓글 목록 조회


    // - 댓글 좋아요
    @PostMapping("/{commentId}/comment-likes")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "관심사 댓글 좋아요", description = "댓글 좋아요를 등록합니다.")
    public CommentLikeDto likeComment(
            @PathVariable UUID commentId,
            @RequestHeader(HEADER_USER_ID) UUID userId
    ) {
        CommentLikeDto response = commentService.likeComment(commentId, userId);
        return response;
    }

    // - 댓글 좋아요 취소
    @DeleteMapping("/{commentId}/comment-likes")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "댓글 좋아요 취소", description = "댓글 좋아요를 취소합니다.")
    public void cancelComment(
            @PathVariable UUID commentId,
            @RequestHeader(HEADER_USER_ID) UUID userId
    ) {
        commentService.cancelLikeComment(commentId, userId);
    }
}
