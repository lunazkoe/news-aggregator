package com.lunazkoe.newsaggregator.domain.article.repository;

import com.lunazkoe.newsaggregator.domain.article.entity.Article;
import com.lunazkoe.newsaggregator.domain.article.entity.Source;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface ArticleRepositoryCustom {
    List<Article> searchArticles(
            String keyword,
            UUID interestId,
            List<Source> sourceIn,
            LocalDateTime publishDateFrom,
            LocalDateTime publishDateTo,
            String orderBy,
            String direction,
            String cursor,
            LocalDateTime after,
            int limit
    );
}
