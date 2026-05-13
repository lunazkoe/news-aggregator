package com.lunazkoe.newsaggregator.domain.comment.controller;

import com.lunazkoe.newsaggregator.domain.comment.dto.request.CommentRegisterRequest;
import com.lunazkoe.newsaggregator.domain.comment.dto.request.CommentUpdateRequest;
import com.lunazkoe.newsaggregator.domain.comment.dto.response.CommentDto;
import com.lunazkoe.newsaggregator.domain.comment.service.CommentService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

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
    // - 댓글 좋아요 취소
}
