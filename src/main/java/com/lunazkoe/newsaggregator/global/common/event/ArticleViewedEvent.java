package com.lunazkoe.newsaggregator.global.common.event;

import com.lunazkoe.newsaggregator.domain.article.entity.Source;
import java.time.LocalDateTime;
import java.util.UUID;

public record ArticleViewedEvent(
        UUID userId, // 기사를 조회한 사용자 ID
        UUID viewedBy,
        UUID articleViewId,
        UUID articleId,
        Source source,
        String sourceUrl,
        String articleTitle,
        LocalDateTime articlePublishedDate,
        String articleSummary,
        int articleCommentCount,
        int articleViewCount,
        LocalDateTime createdAt
) {}