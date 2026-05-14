package com.lunazkoe.newsaggregator.domain.notification.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record SearchNotificationCondition(

        @Schema(description = "커서 값 (마지막으로 조회된 Notification)")
        String cursor,

        @Schema(description = "보조 커서 값 (정렬 기준에 따른 값)")
        String after,

        @Schema(description = "조회 개수", example = "50")
        @Min(value = 1, message = "조회 개수는 1 이상이어야 합니다.")
        @Max(value = 100, message = "한 번에 조회할 수 있는 최대 개수는 100개입니다.")
        Integer limit
) {
    public SearchNotificationCondition {
        if (limit == null || limit <= 0) limit = 50;
    }
}
