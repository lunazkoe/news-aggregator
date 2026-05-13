package com.lunazkoe.newsaggregator.domain.article.dto.request;

import com.lunazkoe.newsaggregator.domain.article.entity.Source;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record ArticleSearchCondition(
        String keyword,                 // 검색어 (제목, 요약)
        UUID interestId,                // 관심사 필터
        List<Source> sourceIn,          // 출처 다중 필터 (NAVER, HANKYUNG 등)
        LocalDateTime publishDateFrom,  // 발행일 시작
        LocalDateTime publishDateTo,    // 발행일 끝
        String orderBy,                 // 정렬 기준 (publishDate, commentCount, viewCount)
        String direction,               // 정렬 방향 (ASC, DESC)
        String cursor,                  // 식별자 커서 (UUID)
        LocalDateTime after,            // 보조 커서 (날짜, 정수 등)
        int limit                       // 페이지 크기
) {
}
