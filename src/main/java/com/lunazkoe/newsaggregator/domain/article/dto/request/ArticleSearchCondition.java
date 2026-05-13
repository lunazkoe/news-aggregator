package com.lunazkoe.newsaggregator.domain.article.dto.request;

import com.lunazkoe.newsaggregator.domain.article.entity.Source;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record ArticleSearchCondition(
        String keyword,                 // 검색어 (제목, 요약)
        UUID interestId,                // 관심사 필터
        List<Source> sourceIn,          // 출처 다중 필터 (NAVER, HANKYUNG 등)
        LocalDateTime publishDateFrom,  // 발행일 시작
        LocalDateTime publishDateTo,    // 발행일 끝

        @Pattern(regexp = "^(publishDate|commentCount|viewCount)$", message = "정렬 기준은 publishDate, commentCount, viewCount 중 하나여야 합니다.")
        String orderBy,                 // 정렬 기준

        @Pattern(regexp = "^(ASC|DESC)$", message = "정렬 방향은 ASC 또는 DESC만 가능합니다.")
        String direction,               // 정렬 방향

        String cursor,                  // 식별자 커서 (UUID)
        String after,                   // 보조 커서 (날짜, 정수 등)

        @Min(value = 1, message = "조회 개수는 1 이상이어야 합니다.")
        @Max(value = 100, message = "한 번에 조회할 수 있는 최대 개수는 100개입니다.")
        Integer limit                   // 페이지 크기 (int -> Integer 로 변경 추천)
) {
    // 파라미터가 비어있을 경우를 대비한 디폴트값 세팅
    public ArticleSearchCondition {
        if (limit == null || limit <= 0) limit = 50;
        if (orderBy == null || orderBy.isBlank()) orderBy = "publishDate"; // 뉴스 기사에 맞는 기본 정렬
        if (direction == null || direction.isBlank()) direction = "DESC";
    }
}