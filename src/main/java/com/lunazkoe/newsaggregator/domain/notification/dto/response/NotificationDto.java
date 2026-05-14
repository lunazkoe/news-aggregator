package com.lunazkoe.newsaggregator.domain.notification.dto.response;

import com.lunazkoe.newsaggregator.domain.notification.entity.Notification;

import java.time.LocalDateTime;
import java.util.UUID;

public record NotificationDto(
        UUID id,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        Boolean confirmed,
        UUID userId,
        String content,
        String resourceType,
        UUID resourceId
) {
    public static NotificationDto from(Notification notification) {
        return new NotificationDto(
                notification.getId(),
                notification.getCreatedAt(),
                notification.getUpdatedAt(),
                notification.isConfirmed(),
                notification.getUser().getId(),
                notification.getContent(),
                notification.getResourceType().name().toLowerCase(),
                notification.getResourceId()
        );
    }
}
