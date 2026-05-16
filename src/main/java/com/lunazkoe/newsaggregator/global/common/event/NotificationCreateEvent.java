package com.lunazkoe.newsaggregator.global.common.event;

import com.lunazkoe.newsaggregator.domain.notification.entity.ResourceType;

import java.util.UUID;

public record NotificationCreateEvent(
        UUID receiverId,            // 알림을 받을 사용자 ID
        String content,             // 알림 내용
        ResourceType resourceType,  // 연관 리소스 타입 (Comment, Interest)
        UUID resourceId             // 연관 리소스 ID
) {
}
