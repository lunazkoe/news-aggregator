package com.lunazkoe.newsaggregator.domain.article.dto.response;

import com.lunazkoe.newsaggregator.domain.article.entity.Article;
import com.lunazkoe.newsaggregator.domain.article.entity.Source;

import java.time.LocalDateTime;
import java.util.UUID;

public record ArticleDto(
        UUID id,
        Source source,
        String sourceUrl,
        String title,
        LocalDateTime publishDate,
        String summary,
        Integer commentCount,
        Integer viewCount,
        Boolean viewedByMe
) {
    public static ArticleDto from(Article article, boolean viewedByMe) {
        return new ArticleDto(
                article.getId(),
                article.getSource(),
                article.getSourceUrl(),
                article.getTitle(),
                article.getPublishDate(),
                article.getSummary(),
                article.getCommentCount(),
                article.getViewCount(),
                viewedByMe
        );
    }
}
