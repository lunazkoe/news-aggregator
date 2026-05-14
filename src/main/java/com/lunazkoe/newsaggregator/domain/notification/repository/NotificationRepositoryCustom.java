package com.lunazkoe.newsaggregator.domain.notification.repository;

import com.lunazkoe.newsaggregator.domain.notification.dto.request.SearchNotificationCondition;
import com.lunazkoe.newsaggregator.domain.notification.entity.Notification;
import com.lunazkoe.newsaggregator.global.common.dto.CursorPageResponse;

import java.util.UUID;

public interface NotificationRepositoryCustom {
    // 미확인 알림 커서 페이징 조회 인터페이스
    CursorPageResponse<Notification> findUnconfirmedNotificationsWithCursor(UUID userId, SearchNotificationCondition condition);
}
