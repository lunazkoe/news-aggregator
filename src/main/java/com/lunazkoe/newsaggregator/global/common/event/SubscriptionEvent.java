package com.lunazkoe.newsaggregator.global.common.event;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record SubscriptionEvent(
        UUID userId, // MongoDB 업데이트를 위한 대상 유저 식별자
        UUID subscriptionId,
        UUID interestId,
        String interestName,
        List<String> interestKeywords,
        int interestSubscriberCount,
        LocalDateTime createdAt
) {}