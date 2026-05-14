package com.lunazkoe.newsaggregator.domain.interest.repository;

import com.lunazkoe.newsaggregator.domain.interest.dto.request.InterestSearchCondition;
import com.lunazkoe.newsaggregator.domain.interest.entity.Interest;
import com.lunazkoe.newsaggregator.global.common.dto.CursorPageResponse;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.StringTemplate;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.lunazkoe.newsaggregator.domain.interest.entity.QInterest.interest;

@Repository
@RequiredArgsConstructor
public class InterestRepositoryImpl implements InterestRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public CursorPageResponse<Interest> searchInterests(InterestSearchCondition condition) {

        List<Interest> interests = queryFactory
                .selectFrom(interest)
                .where(
                        interest.isDeleted.eq(false),
                        containsKeyword(condition.keyword()),
                        cursorCondition(condition.orderBy(), condition.direction(), condition.cursor(), condition.after())
                )
                .orderBy(dynamicOrder(condition.orderBy(), condition.direction()))
                .limit(condition.limit() + 1)
                .fetch();

        boolean hasNext = interests.size() > condition.limit();
        if (hasNext) {
            interests.remove(interests.size() - 1);
        }

        String nextCursor = null;
        String nextAfter = null;

        if (!interests.isEmpty()) {
            Interest lastInterest = interests.get(interests.size() - 1);
            nextCursor = lastInterest.getId().toString();

            nextAfter = switch (condition.orderBy() != null ? condition.orderBy() : "name") {
                case "subscriberCount" -> String.valueOf(lastInterest.getSubscriberCount());
                default -> lastInterest.getName(); // 문자열이므로 그대로 사용
            };
        }

        // TODO 최적화 이걸 꼭 세야할까?
        long totalElements = Optional.ofNullable(queryFactory
                .select(interest.count())
                .from(interest)
                .where(
                        interest.isDeleted.eq(false),
                        containsKeyword(condition.keyword())
                )
                .fetchOne()
        ).orElse(0L);

        return new CursorPageResponse<>(
                interests,
                nextCursor,
                nextAfter,
                condition.limit(),
                totalElements,
                hasNext
        );
    }

    private BooleanExpression containsKeyword(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return null;
        }
        BooleanExpression nameMatch = interest.name.containsIgnoreCase(keyword);

        StringTemplate keywordsAsString = Expressions.stringTemplate(
                "function('array_to_string', {0}, ',')", interest.keywords
        );
        // - function('array_to_string', ...) 부분이 실행되면서 DB 내부적으로 "축구,농구,야구"라는 하나의 문자열로 변환
        // - .containsIgnoreCase("농구")가 이 문자열에 대해 LIKE '%농구%' 쿼리
        BooleanExpression keywordMatch = keywordsAsString.containsIgnoreCase(keyword);

        return nameMatch.or(keywordMatch);
    }

    private BooleanExpression cursorCondition(String orderBy, String direction, String cursor, String after) {
        if (!StringUtils.hasText(cursor)) {
            return null;
        }

        UUID cursorId = UUID.fromString(cursor);
        boolean isAsc = "ASC".equalsIgnoreCase(direction);

        return switch (orderBy != null ? orderBy : "name") {
            case "subscriberCount" -> {
                if (StringUtils.hasText(after)) {
                    int afterSubscriberCount = Integer.parseInt(after);
                    yield isAsc ?
                            interest.subscriberCount.gt(afterSubscriberCount).or(interest.subscriberCount.eq(afterSubscriberCount).and(interest.id.gt(cursorId))) :
                            interest.subscriberCount.lt(afterSubscriberCount).or(interest.subscriberCount.eq(afterSubscriberCount).and(interest.id.lt(cursorId)));
                } else {
                    var subQuery = JPAExpressions.select(interest.subscriberCount).from(interest).where(interest.id.eq(cursorId));
                    yield isAsc ?
                            interest.subscriberCount.gt(subQuery).or(interest.subscriberCount.eq(subQuery).and(interest.id.gt(cursorId))) :
                            interest.subscriberCount.lt(subQuery).or(interest.subscriberCount.eq(subQuery).and(interest.id.lt(cursorId)));
                }
            }

            default -> {
                if (StringUtils.hasText(after)) {
                    String afterName = after;
                    yield isAsc ?
                            interest.name.gt(afterName).or(interest.name.eq(afterName).and(interest.id.gt(cursorId))) :
                            interest.name.lt(afterName).or(interest.name.eq(afterName).and(interest.id.lt(cursorId)));
                } else {
                    var subQuery = JPAExpressions.select(interest.name).from(interest).where(interest.id.eq(cursorId));
                    yield isAsc ?
                            interest.name.gt(subQuery).or(interest.name.eq(subQuery).and(interest.id.gt(cursorId))) :
                            interest.name.lt(subQuery).or(interest.name.eq(subQuery).and(interest.id.lt(cursorId)));
                }
            }
        };
    }

    private OrderSpecifier<?>[] dynamicOrder(String orderBy, String direction) {
        boolean isDesc = "DESC".equalsIgnoreCase(direction);

        if ("subscriberCount".equalsIgnoreCase(orderBy)) {
            return new OrderSpecifier[]{
                    isDesc ? interest.subscriberCount.desc() : interest.subscriberCount.asc(),
                    isDesc ? interest.id.desc() : interest.id.asc()
            };
        } else {
            return new OrderSpecifier[]{
                    isDesc ? interest.name.desc() : interest.name.asc(),
                    isDesc ? interest.id.desc() : interest.id.asc()
            };
        }
    }
}