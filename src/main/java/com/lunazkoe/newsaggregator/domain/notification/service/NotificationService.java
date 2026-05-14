package com.lunazkoe.newsaggregator.domain.notification.service;

import com.lunazkoe.newsaggregator.domain.notification.dto.request.SearchNotificationCondition;
import com.lunazkoe.newsaggregator.domain.notification.dto.response.NotificationDto;
import com.lunazkoe.newsaggregator.domain.notification.entity.Notification;
import com.lunazkoe.newsaggregator.domain.notification.entity.ResourceType;
import com.lunazkoe.newsaggregator.domain.notification.exception.NotificationErrorCode;
import com.lunazkoe.newsaggregator.domain.notification.exception.NotificationException;
import com.lunazkoe.newsaggregator.domain.notification.repository.NotificationRepository;
import com.lunazkoe.newsaggregator.domain.user.entity.User;
import com.lunazkoe.newsaggregator.global.common.dto.CursorPageResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;

    /**
     * 알림 목록 조회 (미확인)
     */
    // TODO: searchNotificationCondition으로 리팩토링 + userId
    @Transactional(readOnly = true)
    public CursorPageResponse<NotificationDto> searchNotificationsNotConfirmed(UUID userId, SearchNotificationCondition condition) {
        log.info("미확인 알림 조회 요청 - userId: {}, limit: {}", userId, condition.limit());

        CursorPageResponse<Notification> pageResponse = notificationRepository.findUnconfirmedNotificationsWithCursor(userId, condition);

        List<NotificationDto> dtoList = pageResponse.content().stream()
                .map(NotificationDto::from)
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
     * 전체 알림 확인
     */
    @Transactional
    public void confirmAllNotifications(UUID userId) {
        log.info("전체 알림 확인 요청 - userId: {}", userId);
        int rowCount = notificationRepository.markAllAsRead(userId, LocalDateTime.now());
        log.info("총 {}건의 알림 확인 처리 완료", rowCount);
    }

    /**
     * 알림 확인
     */
    @Transactional
    public void confirmNotification(UUID notificationId, UUID userId) {
        log.info("단일 알림 확인 요청 - notificationId: {}, userId: {}", notificationId, userId);

        Notification foundNotification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new NotificationException(NotificationErrorCode.NOTIFICATION_NOT_FOUND));

        if (!foundNotification.getUser().getId().equals(userId)) {
            throw new NotificationException(NotificationErrorCode.FORBIDDEN_ACCESS);
        }

        // TODO: 동시성 문제 발생 가능성
        foundNotification.confirm();
    }

    // TODO: 내부용: 추후 Event Listener 등에서 호출됨
    @Transactional
    public void createNotification(User user, String content, ResourceType type, UUID resourceId) {
        Notification notification = Notification.builder()
                .user(user)
                .content(content)
                .resourceType(type)
                .resourceId(resourceId)
                .build();
        notificationRepository.save(notification);
        log.info("새로운 알림 생성 완료 - userId: {}, type: {}", user.getId(), type);
    }
}
