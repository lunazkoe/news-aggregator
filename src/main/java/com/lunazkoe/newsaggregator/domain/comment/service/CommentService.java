package com.lunazkoe.newsaggregator.domain.comment.service;

import com.lunazkoe.newsaggregator.domain.article.entity.Article;
import com.lunazkoe.newsaggregator.domain.article.exception.ArticleErrorCode;
import com.lunazkoe.newsaggregator.domain.article.exception.ArticleException;
import com.lunazkoe.newsaggregator.domain.article.repository.ArticleRepository;
import com.lunazkoe.newsaggregator.domain.comment.dto.request.CommentRegisterRequest;
import com.lunazkoe.newsaggregator.domain.comment.dto.request.CommentUpdateRequest;
import com.lunazkoe.newsaggregator.domain.comment.dto.response.CommentDto;
import com.lunazkoe.newsaggregator.domain.comment.dto.response.CommentLikeDto;
import com.lunazkoe.newsaggregator.domain.comment.entity.Comment;
import com.lunazkoe.newsaggregator.domain.comment.entity.CommentLike;
import com.lunazkoe.newsaggregator.domain.comment.exception.CommentErrorCode;
import com.lunazkoe.newsaggregator.domain.comment.exception.CommentException;
import com.lunazkoe.newsaggregator.domain.comment.repository.CommentLikeRepository;
import com.lunazkoe.newsaggregator.domain.comment.repository.CommentRepository;
import com.lunazkoe.newsaggregator.domain.user.entity.User;
import com.lunazkoe.newsaggregator.domain.user.exception.UserErrorCode;
import com.lunazkoe.newsaggregator.domain.user.exception.UserException;
import com.lunazkoe.newsaggregator.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final ArticleRepository articleRepository;
    private final CommentLikeRepository commentLikeRepository;

    @Transactional
    public CommentDto register(CommentRegisterRequest request) {
        log.info("Registering new comment for article: {}, user: {}", request.articleId(), request.userId());

        Article foundArticle = articleRepository.findById(request.articleId())
                .orElseThrow(() -> new ArticleException(ArticleErrorCode.ARTICLE_NOT_FOUND, Map.of("id", request.articleId())));

        User foundUser = userRepository.findById(request.userId())
                .orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND, Map.of("id", request.userId())));

        Comment newComment = Comment.builder()
                .article(foundArticle)
                .user(foundUser)
                .content(request.content())
                .build();

        Comment savedComment = commentRepository.save(newComment);

        // - 등록 직후이므로 종아요는 누르지 않은 상태
        return CommentDto.from(savedComment, false);
    }

    @Transactional
    public CommentDto update(UUID commentId, CommentUpdateRequest request) {
        log.info("Updating comment ID: {}", commentId);

        Comment foundComment = findCommentOrThrow(commentId);

        foundComment.updateContent(request.content());

        // 이때는 좋아요 여부를 확인해야함
        // - TODO: 종아요 여부는 아직 처리 안 함
        return CommentDto.from(foundComment, false);
    }

    @Transactional
    public void softDelete(UUID commentId) {
        log.info("Logically deleting comment ID: {}", commentId);

        Comment foundComment = findCommentOrThrow(commentId);

        foundComment.softDelete();
    }

    @Transactional
    public void hardDelete(UUID commentId) {
        log.info("Hard deleting comment ID: {}", commentId);

        Comment foundComment = findCommentOrThrow(commentId);

        commentRepository.delete(foundComment);
    }

    // TODO: 좋아요 / 종하요 취소 / 댓글 목록 조회는 좀 미루기
    @Transactional
    public CommentLikeDto likeComment(UUID commentId, UUID userId) {
        log.info("댓글 좋아요 요청 처리 시작 - commentId: {}, userId: {}", commentId, userId);

        Comment foundComment = commentRepository.findById(commentId)
                .orElseThrow(() -> new CommentException(CommentErrorCode.COMMENT_NOT_FOUND));

        User foundUser = userRepository.findById(userId)
                .orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND));

        // 근데 아마 내부 구현상 좋아요를 한 번 더 누르면 그렇지 않을 것 같지만 그래도 요청 자체를 차단
        if (commentLikeRepository.existsByUserIdAndCommentId(userId, commentId)) {
            throw new CommentException(CommentErrorCode.ALREADY_LIKED);
        }

        CommentLike newCommentLike = CommentLike.builder()
                .comment(foundComment)
                .user(foundUser) // 좋아요를 누른 사람 (댓글을 쓴 사람이 아님)
                .build();

        commentLikeRepository.save(newCommentLike);
        foundComment.increaseLikeCount();

        log.info("댓글 좋아요 완료 - likeId: {}", newCommentLike.getId());
        return CommentLikeDto.from(newCommentLike);
    }

    @Transactional
    public void cancelLikeComment(UUID commentId, UUID userId) {
        log.info("댓글 좋아요 취소 요청 처리 시작 - commentId: {}, userId: {}", commentId, userId);

        CommentLike commentLike = commentLikeRepository.findByUserIdAndCommentId(userId, commentId)
                .orElseThrow(() -> new CommentException(CommentErrorCode.LIKE_NOT_FOUND));

        Comment comment = commentLike.getComment();
        comment.decreaseLikeCount(); // 더티 체킹

        commentLikeRepository.delete(commentLike);
        log.info("댓글 좋아요 취소 완료 - commentId: {}", commentId);
    }

    private Comment findCommentOrThrow(UUID commentId) {
        return commentRepository.findById(commentId)
                .orElseThrow(() -> {
                    log.warn("Comment not found with ID: {}", commentId);
                    return new CommentException(CommentErrorCode.COMMENT_NOT_FOUND, Map.of("id", commentId));
                });
    }

}
