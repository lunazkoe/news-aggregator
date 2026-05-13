package com.lunazkoe.newsaggregator.domain.comment.repository;

import com.lunazkoe.newsaggregator.domain.comment.dto.request.SearchCommentCondition;
import com.lunazkoe.newsaggregator.domain.comment.entity.Comment;
import com.lunazkoe.newsaggregator.domain.comment.entity.QComment;
import com.lunazkoe.newsaggregator.global.common.dto.CursorPageResponse;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.lunazkoe.newsaggregator.domain.article.entity.QArticle.article;
import static com.lunazkoe.newsaggregator.domain.comment.entity.QComment.*;

@RequiredArgsConstructor
public class CommentRepositoryImpl implements CommentRepositoryCustom{

    private final JPAQueryFactory queryFactory;

    @Override
    public CursorPageResponse<Comment> searchComments(SearchCommentCondition condition) {

        List<Comment> comments = queryFactory
                .selectFrom(comment)
                .where(
                        searchArticleId(condition.articleId()),
                        isNotDeleted(),
                        cursorCondition(condition.orderBy(), condition.direction(), condition.cursor(), condition.after())
                )
                .orderBy(dynamicOrder(condition.orderBy(), condition.direction()))
                .limit(condition.limit() + 1)
                .fetch();

        boolean hasNext = comments.size() > condition.limit();
        if (hasNext) {
            comments.remove(comments.size() - 1);
        }

        String nextCursor = null;
        String nextAfter = null;

        if (!comments.isEmpty()) {
            Comment lastComment = comments.get(comments.size() - 1);
            nextCursor = lastComment.getId().toString();

            nextAfter = switch (condition.orderBy() != null ? condition.orderBy() : "createdAt") {
                case "likeCount" -> String.valueOf(lastComment.getLikeCount());
                default -> lastComment.getCreatedAt().toString();
            };
        }


        long totalElements = Optional.ofNullable(queryFactory
                .select(comment.count())
                .from(comment)
                .where(
                        comment.articleId.eq(condition.articleId()),
                        comment.isDeleted.eq(false)
                )
                .fetchOne()
        ).orElse(0L);

        return new CursorPageResponse<>(
                comments,
                nextCursor,
                nextAfter,
                condition.limit(),
                totalElements,
                hasNext
        );
    }

    private BooleanExpression searchArticleId(UUID articleId) {
        if (articleId == null) {
            return null;
        }
        return comment.articleId.eq(articleId);
    }

    private BooleanExpression isNotDeleted() {
        return comment.isDeleted.eq(false);
    }

    private BooleanExpression cursorCondition(String orderBy, String direction, String cursor, String after) {
        if (!StringUtils.hasText(cursor)) {
            return null;
        }

        UUID cursorId = UUID.fromString(cursor);
        boolean isAsc = "ASC".equalsIgnoreCase(direction);

        return switch (orderBy != null ? orderBy : "createdAt") {
            case "likeCount" -> {
                if (StringUtils.hasText(after)) {
                    int afterLikeCount = Integer.parseInt(after);
                    yield isAsc ?
                            comment.likeCount.gt(afterLikeCount).or(comment.likeCount.eq(afterLikeCount).and(comment.id.gt(cursorId))) :
                            comment.likeCount.lt(afterLikeCount).or(comment.likeCount.eq(afterLikeCount).and(comment.id.lt(cursorId)));
                } else {
                    var subQuery = JPAExpressions.select(comment.likeCount).from(comment).where(comment.id.eq(cursorId));
                    yield isAsc ?
                            comment.likeCount.gt(subQuery).or(comment.likeCount.eq(subQuery).and(comment.id.gt(cursorId))) :
                            comment.likeCount.lt(subQuery).or(comment.likeCount.eq(subQuery).and(comment.id.lt(cursorId)));
                }
            }

            default -> {
                if (!StringUtils.hasText(after)) yield null; // 생성 일시는 반드시 있어야함
                LocalDateTime afterDate;
                try {
                    afterDate = ZonedDateTime.parse(after).toLocalDateTime();
                } catch (Exception e) {
                    afterDate = LocalDateTime.parse(after);
                }
                yield isAsc ?
                        comment.createdAt.gt(afterDate).or(comment.createdAt.eq(afterDate).and(comment.id.gt(cursorId))) :
                        comment.createdAt.lt(afterDate).or(comment.createdAt.eq(afterDate).and(comment.id.lt(cursorId)));
            }
        };
    }

    // 동적 정렬 로직 (반드시 cursorCondition의 논리와 짝을 이루어야 함)
    private OrderSpecifier<?>[] dynamicOrder(String orderBy, String direction) {
        Order orderDirection = "ASC".equalsIgnoreCase(direction) ? Order.ASC : Order.DESC;

        return switch (orderBy != null ? orderBy : "createdAt") {
            case "likeCount" -> new OrderSpecifier[]{new OrderSpecifier<>(orderDirection, comment.likeCount), new OrderSpecifier<>(orderDirection, comment.id)};
            default -> new OrderSpecifier[]{new OrderSpecifier<>(orderDirection, comment.createdAt), new OrderSpecifier<>(orderDirection, comment.id)};
        };
    }
}
