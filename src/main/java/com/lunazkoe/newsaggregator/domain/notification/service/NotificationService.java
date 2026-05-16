package com.lunazkoe.newsaggregator.domain.notification.service;

import com.lunazkoe.newsaggregator.domain.notification.dto.request.SearchNotificationCondition;
import com.lunazkoe.newsaggregator.domain.notification.dto.response.NotificationDto;
import com.lunazkoe.newsaggregator.domain.notification.entity.Notification;
import com.lunazkoe.newsaggregator.domain.notification.entity.ResourceType;
import com.lunazkoe.newsaggregator.domain.notification.exception.NotificationErrorCode;
import com.lunazkoe.newsaggregator.domain.notification.exception.NotificationException;
import com.lunazkoe.newsaggregator.domain.notification.repository.NotificationRepository;
import com.lunazkoe.newsaggregator.domain.user.entity.User;
import com.lunazkoe.newsaggregator.domain.user.repository.UserRepository;
import com.lunazkoe.newsaggregator.global.common.dto.CursorPageResponse;
import com.lunazkoe.newsaggregator.global.common.event.NotificationCreateEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

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
    public void createNotification(NotificationCreateEvent event) {
        // 수신자 검증: 회원이 탈퇴했거나 없는 경우 즉시 알림 생성 무시
        User receiver = userRepository.findById(event.receiverId())
                .orElse(null);

        if (receiver == null) {
            log.warn("[Notification] 수신자를 찾을 수 없거나 탈퇴한 회원입니다. receiverId: {}", receiver.getId());
            return;
        }

        // 알림 엔티티 생성
        Notification notification = Notification.builder()
                .user(receiver)
                .content(event.content())
                .resourceType(event.resourceType())
                .resourceId(event.resourceId())
                .build();

        notificationRepository.save(notification);
        log.info("[Notification Created] 알림 저장 완료. notificationId: {}, receiverId: {}",
                notification.getId(), receiver.getId());
    }
}
