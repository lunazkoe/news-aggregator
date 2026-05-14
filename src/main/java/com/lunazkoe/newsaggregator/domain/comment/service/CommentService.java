package com.lunazkoe.newsaggregator.domain.comment.service;

import com.lunazkoe.newsaggregator.domain.article.entity.Article;
import com.lunazkoe.newsaggregator.domain.article.exception.ArticleErrorCode;
import com.lunazkoe.newsaggregator.domain.article.exception.ArticleException;
import com.lunazkoe.newsaggregator.domain.article.repository.ArticleRepository;
import com.lunazkoe.newsaggregator.domain.comment.dto.request.CommentRegisterRequest;
import com.lunazkoe.newsaggregator.domain.comment.dto.request.CommentUpdateRequest;
import com.lunazkoe.newsaggregator.domain.comment.dto.request.SearchCommentCondition;
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
import com.lunazkoe.newsaggregator.global.common.dto.CursorPageResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
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

    /**
     * 댓글 목록 조회
     */
    @Transactional
    public CursorPageResponse<CommentDto> searchComments(SearchCommentCondition condition, UUID requestUserId) {
        log.info("Searching comments with condition: {}", condition);

        CursorPageResponse<Comment> pageResponse = commentRepository.searchComments(condition);

        // TODO: N+1 문제 발생 예상 지점
        List<CommentDto> dtoList = pageResponse.content().stream()
                .map(comment -> {
                    boolean likedByMe = commentLikeRepository.existsByUserIdAndCommentId(requestUserId, comment.getId());
                    return CommentDto.from(comment, likedByMe);
                })
                .toList();

        return new CursorPageResponse<>(
                dtoList,
                pageResponse.nextCursor(),
                pageResponse.nextAfter(),
                pageResponse.size(),
                pageResponse.totalElements(),
                pageResponse.hasNext()
        );
    }

    /**
     * 댓글 등록
     */
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

        // 기사 댓글 수 증가
        foundArticle.increaseCommentCount();

        return CommentDto.from(savedComment, false);
        // - 등록 직후이므로 좋아요는 누르지 않은 상태
        // - 좋아요 누르는 건 따로 처리
    }

    /**
     * 관심사 댓글 좋아요
     */
    @Transactional
    public CommentLikeDto likeComment(UUID commentId, UUID userId) {
        log.info("댓글 좋아요 요청 처리 시작 - commentId: {}, userId: {}", commentId, userId);

        Comment foundComment = commentRepository.findById(commentId)
                .orElseThrow(() -> new CommentException(CommentErrorCode.COMMENT_NOT_FOUND));

        User foundUser = userRepository.findById(userId)
                .orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND));

        // TODO: 내부 구현상 좋아요를 이미 눌렀으면 눌렀다는 것이 표현되어서 다시 누르면 취소 요청이 날아오겠지만 로직적으로 일단 방어 => 추후 개선
        if (commentLikeRepository.existsByUserIdAndCommentId(userId, commentId)) {
            throw new CommentException(CommentErrorCode.ALREADY_LIKED);
        }

        CommentLike newCommentLike = CommentLike.builder()
                .comment(foundComment)
                .user(foundUser) // 주의: 좋아요를 누른 사람 (댓글을 쓴 사람이 아님)
                .build();

        commentLikeRepository.save(newCommentLike);

        // 댓글 좋아요 수 증가
        foundComment.increaseLikeCount();

        log.info("댓글 좋아요 완료 - likeId: {}", newCommentLike.getId());
        return CommentLikeDto.from(newCommentLike);
        // - 완성된 객체기 때문에 추가 쿼리가 발생하는 그런 문제는 없을 것으로 예상
    }

    /**
     * 댓글 좋아요 취소
     */
    @Transactional
    public void cancelLikeComment(UUID commentId, UUID userId) {
        log.info("댓글 좋아요 취소 요청 처리 시작 - commentId: {}, userId: {}", commentId, userId);

        // TODO: 애초에 좋아요를 누르지 않았으면 누르지 않아다고 클라이언트에 표시될 것이고 누르면 좋아요 요청으로 날아오겠지만 일단 방어
        CommentLike commentLike = commentLikeRepository.findByUserIdAndCommentId(userId, commentId)
                .orElseThrow(() -> new CommentException(CommentErrorCode.LIKE_NOT_FOUND));

        Comment comment = commentLike.getComment();

        // 댓글 좋아요 수 감소
        comment.decreaseLikeCount();

        commentLikeRepository.delete(commentLike);
        log.info("댓글 좋아요 취소 완료 - commentId: {}", commentId);
    }

    /**
     * 댓글 논리 삭제
     */
    @Transactional
    public void softDelete(UUID commentId) {
        log.info("Logically deleting comment ID: {}", commentId);

        Comment foundComment = findCommentOrThrow(commentId);

        // 기사 댓글 수 감소
        // - TODO: 이 때 추가 쿼리 발생할 것으로 예상 최적화 가능할지도?
        foundComment.getArticle().decreaseCommentCount();

        foundComment.softDelete();
    }

    /**
     * 댓글 정보 수정
     */
    @Transactional
    public CommentDto update(UUID commentId, CommentUpdateRequest request, UUID requestUserId) {
        log.info("Updating comment ID: {}", commentId);

        Comment foundComment = findCommentOrThrow(commentId);

        foundComment.updateContent(request.content());

        boolean likedByMe = commentLikeRepository.existsByUserIdAndCommentId(requestUserId, commentId);

        return CommentDto.from(foundComment, likedByMe);
    }

    /**
     * 댓글 물리 삭제
     */
    // TODO: 물리 삭제 외래키 때문에 삭제 문제에 대한 로직 처리 미흡 추후 개선사항
    @Transactional
    public void hardDelete(UUID commentId) {
        log.info("Hard deleting comment ID: {}", commentId);

        Comment foundComment = findCommentOrThrow(commentId);

        // 기사 댓글 수 감소
        // - TODO: 이 때 추가 쿼리 발생할 것으로 예상 최적화 가능할지도?
        foundComment.getArticle().decreaseCommentCount();

        commentRepository.delete(foundComment);
    }

    private Comment findCommentOrThrow(UUID commentId) {
        return commentRepository.findById(commentId)
                .orElseThrow(() -> {
                    log.warn("Comment not found with ID: {}", commentId);
                    return new CommentException(CommentErrorCode.COMMENT_NOT_FOUND, Map.of("id", commentId));
                });
    }
}
