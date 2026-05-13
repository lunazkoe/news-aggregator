package com.lunazkoe.newsaggregator.domain.article.repository;

import com.lunazkoe.newsaggregator.domain.article.dto.request.ArticleSearchCondition;
import com.lunazkoe.newsaggregator.domain.article.entity.Article;
import com.lunazkoe.newsaggregator.domain.article.entity.Source;
import com.lunazkoe.newsaggregator.global.common.dto.CursorPageResponse;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static com.lunazkoe.newsaggregator.domain.article.entity.QArticle.article;

@RequiredArgsConstructor
public class ArticleRepositoryImpl implements ArticleRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public CursorPageResponse<Article> searchArticles(ArticleSearchCondition condition) {
        // 1. LIMIT + 1을 조회하여 다음 페이지가 있는지(hasNext) 확인합니다.
        int limit = condition.limit();
        List<Article> articles = queryFactory
                .selectFrom(article)
                .where(
                        isNotDeleted(),
                        keywordContains(condition.keyword()),
                        sourceIn(condition.sourceIn()),
                        publishDateBetween(condition.publishDateFrom(), condition.publishDateTo()),
                        // 정렬 기준과 커서 값을 함께 넘겨서 정확하게 페이징 처리
                        cursorCondition(condition.orderBy(), condition.direction(), condition.cursor(), condition.after())
                )
                .orderBy(createOrderSpecifier(condition.orderBy(), condition.direction()))
                .limit(limit + 1)
                .fetch();

        // 2. hasNext 및 nextCursor 로직 계산
        boolean hasNext = articles.size() > limit;
        String nextCursor = null;
        LocalDateTime nextAfter = null;

        if (hasNext) {
            articles.remove(limit); // 실제 응답에서는 초과분(1개)을 제거
        }

        if (!articles.isEmpty()) {
            Article lastArticle = articles.get(articles.size() - 1);
            nextCursor = lastArticle.getId().toString();
            nextAfter = lastArticle.getPublishDate(); // 날짜 정렬을 위한 보조 커서 유지
        }

        // 3. 전체 카운트는 별도 쿼리로 분리하여 성능 최적화
        long totalElements = queryFactory
                .select(article.count())
                .from(article)
                .where(
                        isNotDeleted(),
                        keywordContains(condition.keyword()),
                        sourceIn(condition.sourceIn()),
                        publishDateBetween(condition.publishDateFrom(), condition.publishDateTo())
                )
                .fetchOne();

        return new CursorPageResponse<>(
                articles,
                nextCursor,
                nextAfter,
                limit,
                totalElements,
                hasNext
        );
    }

    // --- 동적 WHERE 조건 메서드들 ---
    private BooleanExpression isNotDeleted() {
        return article.isDeleted.eq(false);
    }

    private BooleanExpression keywordContains(String keyword) {
        if (!StringUtils.hasText(keyword)) {
            return null;
        }
        return article.title.containsIgnoreCase(keyword)
                .or(article.summary.containsIgnoreCase(keyword));
    }

    private BooleanExpression sourceIn(List<Source> sourceIn) {
        if (sourceIn == null || sourceIn.isEmpty()) {
            return null;
        }
        return article.source.in(sourceIn);
    }

    private BooleanExpression publishDateBetween(LocalDateTime from, LocalDateTime to) {
        if (from != null && to != null) {
            return article.publishDate.between(from, to);
        } else if (from != null) {
            return article.publishDate.goe(from);
        } else if (to != null) {
            return article.publishDate.loe(to);
        }
        return null;
    }

    // 커서 기반 페이징의 핵심 로직 (정렬 기준별 분기 & JPAExpressions 서브쿼리 활용)
    private BooleanExpression cursorCondition(String orderBy, String direction, String cursor, LocalDateTime after) {
        // 첫 페이지 요청이거나 커서 값이 없으면 조건 무시 (처음부터 조회)
        if (!StringUtils.hasText(cursor)) {
            return null;
        }

        UUID cursorId = UUID.fromString(cursor);
        boolean isAsc = "ASC".equalsIgnoreCase(direction);

        return switch (orderBy != null ? orderBy : "publishDate") {
            case "commentCount" -> {
                // 서브쿼리로 이전 기사의 댓글수를 가져옴 (JPAExpressions 사용 필수)
                var subQuery = JPAExpressions.select(article.commentCount)
                        .from(article)
                        .where(article.id.eq(cursorId));
                yield isAsc ?
                        article.commentCount.gt(subQuery).or(article.commentCount.eq(subQuery).and(article.id.gt(cursorId))) :
                        article.commentCount.lt(subQuery).or(article.commentCount.eq(subQuery).and(article.id.lt(cursorId)));
            }

            case "viewCount" -> {
                // 서브쿼리로 이전 기사의 조회수를 가져옴
                var subQuery = JPAExpressions.select(article.viewCount)
                        .from(article)
                        .where(article.id.eq(cursorId));
                yield isAsc ?
                        article.viewCount.gt(subQuery).or(article.viewCount.eq(subQuery).and(article.id.gt(cursorId))) :
                        article.viewCount.lt(subQuery).or(article.viewCount.eq(subQuery).and(article.id.lt(cursorId)));
            }

            // 기본값: publishDate 정렬
            default -> {
                if (after == null) yield null; // 발행일 정렬인데 after 값이 없으면 페이징 불가

                yield isAsc ?
                        article.publishDate.gt(after).or(article.publishDate.eq(after).and(article.id.gt(cursorId))) :
                        article.publishDate.lt(after).or(article.publishDate.eq(after).and(article.id.lt(cursorId)));
            }
        };
    }

    // 동적 정렬 로직 (반드시 cursorCondition의 논리와 짝을 이루어야 함)
    private OrderSpecifier<?>[] createOrderSpecifier(String orderBy, String direction) {
        Order orderDirection = "ASC".equalsIgnoreCase(direction) ? Order.ASC : Order.DESC;

        // 주 정렬 조건이 같을 경우(동점자 발생), 반드시 고유값인 id를 보조 정렬 조건으로 추가해야 함
        return switch (orderBy != null ? orderBy : "publishDate") {
            case "commentCount" -> new OrderSpecifier[]{new OrderSpecifier<>(orderDirection, article.commentCount), new OrderSpecifier<>(orderDirection, article.id)};
            case "viewCount" -> new OrderSpecifier[]{new OrderSpecifier<>(orderDirection, article.viewCount), new OrderSpecifier<>(orderDirection, article.id)};
            default -> new OrderSpecifier[]{new OrderSpecifier<>(orderDirection, article.publishDate), new OrderSpecifier<>(orderDirection, article.id)};
        };
    }
}