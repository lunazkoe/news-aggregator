package com.lunazkoe.newsaggregator.domain.notification.repository;

import com.lunazkoe.newsaggregator.domain.notification.dto.request.SearchNotificationCondition;
import com.lunazkoe.newsaggregator.domain.notification.entity.Notification;
import com.lunazkoe.newsaggregator.global.common.dto.CursorPageResponse;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.lunazkoe.newsaggregator.domain.notification.entity.QNotification.notification;

@Repository
@RequiredArgsConstructor
public class NotificationRepositoryImpl implements NotificationRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public CursorPageResponse<Notification> findUnconfirmedNotificationsWithCursor(UUID userId, SearchNotificationCondition condition) {

        List<Notification> notifications = queryFactory
                .selectFrom(notification)
                .where(
                        notification.user.id.eq(userId),
                        notification.confirmed.eq(false),
                        cursorCondition(condition.cursor(), condition.after())
                )
                .orderBy(notification.createdAt.desc(), notification.id.desc())
                .limit(condition.limit() + 1) // hasNext 판별을 위해 limit + 1개 조회
                .fetch();

        boolean hasNext = notifications.size() > condition.limit();
        if (hasNext) {
            notifications.remove(notifications.size() - 1);
        }

        String nextCursor = null;
        String nextAfter = null;

        if (!notifications.isEmpty()) {
            Notification lastElement = notifications.get(notifications.size() - 1);
            nextCursor = lastElement.getId().toString();
            nextAfter = lastElement.getCreatedAt().toString();
        }

        long totalElements = Optional.ofNullable(queryFactory
                .select(notification.count())
                .from(notification)
                .where(
                        notification.user.id.eq(userId),
                        notification.confirmed.eq(false)
                )
                .fetchOne()
        ).orElse(0L);


        return new CursorPageResponse<>(
                notifications,
                nextCursor,
                nextAfter,
                condition.limit(),
                totalElements,
                hasNext
        );
    }

    private BooleanExpression cursorCondition(String cursor, String after) {
        // 값이 없다는 것은 첫 번째 데이터를 조회하는 상황
        if (cursor == null || after == null) {
            return null;
        }

        LocalDateTime parseAfter = LocalDateTime.parse(after);

        return notification.createdAt.lt(parseAfter)
                .or(notification.createdAt.eq(parseAfter).and(notification.id.lt(UUID.fromString(cursor))));
    }
}
