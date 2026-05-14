package com.lunazkoe.newsaggregator.domain.article.dto.response;

import com.lunazkoe.newsaggregator.domain.article.entity.ArticleView;

import java.time.LocalDateTime;
import java.util.UUID;

public record ArticleViewDto(
        UUID id,
        UUID viewedBy,
        LocalDateTime createdAt,
        UUID articleId,
        String source,
        String sourceUrl,
        String articleTitle,
        LocalDateTime articlePublishedDate,
        String articleSummary,
        Integer articleCommentCount,
        Integer articleViewCount
) {
    public static ArticleViewDto from(ArticleView articleView) {
        return new ArticleViewDto(
                articleView.getId(),
                articleView.getUser().getId(),
                articleView.getCreatedAt(),
                articleView.getArticle().getId(),
                articleView.getArticle().getSource().name(),
                articleView.getArticle().getSourceUrl(),
                articleView.getArticle().getTitle(),
                articleView.getArticle().getPublishDate(),
                articleView.getArticle().getSummary(),
                articleView.getArticle().getCommentCount(),
                articleView.getArticle().getViewCount()
        );
    }
}
