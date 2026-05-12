package com.lunazkoe.newsaggregator.domain.interest.dto.response;

import com.lunazkoe.newsaggregator.domain.interest.entity.Interest;

import java.util.List;
import java.util.UUID;

public record InterestDto(
        UUID id,
        String name,
        List<String> keywords,
        Integer subscriberCount,
        Boolean subscribedByMe
) {
    // 팩토리 메서드를 만들어두면 Service 계층에서 변환하기 매우 편해집니다.
    public static InterestDto from(Interest interest, boolean subscribedByMe) {
        return new InterestDto(
                interest.getId(),
                interest.getName(),
                interest.getKeywords(),
                interest.getSubscriberCount(),
                subscribedByMe
        );
    }
}
