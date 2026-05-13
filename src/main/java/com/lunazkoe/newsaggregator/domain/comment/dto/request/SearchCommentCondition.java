package com.lunazkoe.newsaggregator.domain.comment.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;

import java.util.UUID;

public record SearchCommentCondition(
        @Schema(description = "기사 ID (특정 기사의 댓글만 조회할 경우)", example = "123e4567-e89b-12d3-a456-426614174000")
        UUID articleId,

        @Schema(description = "정렬 속성 (createdAt, likeCount)", example = "createdAt")
        @Pattern(regexp = "^(createdAt|likeCount)$", message = "정렬 기준은 createdAt 또는 likeCount만 가능합니다.")
        String orderBy,

        @Schema(description = "정렬 방향 (ASC, DESC)", example = "DESC")
        @Pattern(regexp = "^(ASC|DESC)$", message = "정렬 방향은 ASC 또는 DESC만 가능합니다.")
        String direction,

        @Schema(description = "커서 값 (마지막으로 조회된 Comment ID)")
        String cursor,

        @Schema(description = "보조 커서 값 (정렬 기준에 따른 값)")
        String after,

        @Schema(description = "조회 개수", example = "50")
        @Min(value = 1, message = "조회 개수는 1 이상이어야 합니다.")
        @Max(value = 100, message = "한 번에 조회할 수 있는 최대 개수는 100개입니다.")
        Integer limit
) {
    // 바인딩 -> 생성자 실행(디폴트 세팅) -> @Valid 검증 실행
    public SearchCommentCondition {
        if (limit == null || limit <= 0) limit = 50;
        if (orderBy == null || orderBy.isBlank()) orderBy = "createdAt"; // 댓글의 기본 정렬은 최신순
        if (direction == null || direction.isBlank()) direction = "DESC";
    }
}