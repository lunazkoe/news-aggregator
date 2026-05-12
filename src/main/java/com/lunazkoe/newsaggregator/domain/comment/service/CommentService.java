package com.lunazkoe.newsaggregator.domain.comment.service;

import com.lunazkoe.newsaggregator.domain.comment.dto.request.CommentRegisterRequest;
import com.lunazkoe.newsaggregator.domain.comment.dto.request.CommentUpdateRequest;
import com.lunazkoe.newsaggregator.domain.comment.dto.response.CommentDto;
import com.lunazkoe.newsaggregator.domain.comment.entity.Comment;
import com.lunazkoe.newsaggregator.domain.comment.exception.CommentErrorCode;
import com.lunazkoe.newsaggregator.domain.comment.exception.CommentException;
import com.lunazkoe.newsaggregator.domain.comment.repository.CommentRepository;
import com.lunazkoe.newsaggregator.domain.user.entity.User;
import com.lunazkoe.newsaggregator.domain.user.exception.UserErrorCode;
import com.lunazkoe.newsaggregator.domain.user.exception.UserException;
import com.lunazkoe.newsaggregator.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentService {

    private final CommentRepository commentRepository;
    private final UserRepository userRepository;

    @Transactional
    public CommentDto register(CommentRegisterRequest request) {
        log.info("Registering new comment for article: {}, user: {}", request.articleId(), request.userId());

        Comment newComment = Comment.builder()
                .articleId(request.articleId())
                .userId(request.userId())
                .content(request.content())
                .build();

        Comment savedComment = commentRepository.save(newComment);

        User foundUser = userRepository.findById(request.userId())
                .orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND));

        // - 등록 직후이므로 종아요는 누르지 않은 상태
        return CommentDto.from(savedComment, foundUser.getNickname(), false);
    }

    @Transactional
    public CommentDto update(UUID commentId, UUID requestUserId, CommentUpdateRequest request) {
        log.info("Updating comment ID: {}, requested by user: {}", commentId, requestUserId);

        Comment foundComment = findCommentOrThrow(commentId);

        // 수정 권한 확인
        validateCommentOwner(foundComment, requestUserId);

        foundComment.updateContent(request.content());

        User foundUser = userRepository.findById(requestUserId)
                .orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND));
        // 이때는 좋아요 여부를 확인해야함
        // - TODO: 종아요 여부는 아직 처리 안 함
        return CommentDto.from(foundComment, foundUser.getNickname(), false);
    }

    @Transactional
    public void softDelete(UUID commentId, UUID requestUserId) {
        log.info("Logically deleting comment ID: {}", commentId);

        Comment foundComment = findCommentOrThrow(commentId);
        validateCommentOwner(foundComment, requestUserId);

        foundComment.softDelete();
    }

    @Transactional
    public void hardDelete(UUID commentId) {
        log.info("Hard deleting comment ID: {}", commentId);

        Comment foundComment = findCommentOrThrow(commentId);

        commentRepository.delete(foundComment);
    }

    // TODO: 좋아요 / 종하요 취소 / 댓글 목록 조회는 좀 미루기

    private void validateCommentOwner(Comment foundComment, UUID requestUserId) {
        if (!foundComment.getUserId().equals(requestUserId)) {
            log.warn("User {} attempted to modify comment {} belonging to user {}", requestUserId, foundComment.getId(), foundComment.getUserId());
            throw new CommentException(CommentErrorCode.FORBIDDEN);
        }
    }

    private Comment findCommentOrThrow(UUID commentId) {
        return commentRepository.findById(commentId)
                .orElseThrow(() -> {
                    log.warn("Comment not found with ID: {}", commentId);
                    return new CommentException(CommentErrorCode.COMMENT_NOT_FOUND);
                });
    }

}
