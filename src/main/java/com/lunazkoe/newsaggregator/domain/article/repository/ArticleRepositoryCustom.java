package com.lunazkoe.newsaggregator.domain.article.repository;

import com.lunazkoe.newsaggregator.domain.article.dto.request.ArticleSearchCondition;
import com.lunazkoe.newsaggregator.domain.article.entity.Article;
import com.lunazkoe.newsaggregator.domain.article.entity.Source;
import com.lunazkoe.newsaggregator.global.common.dto.CursorPageResponse;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface ArticleRepositoryCustom {
    CursorPageResponse<Article> searchArticles(ArticleSearchCondition condition);
}
