package com.lunazkoe.newsaggregator.domain.article.repository;

import com.lunazkoe.newsaggregator.domain.article.dto.request.ArticleSearchCondition;
import com.lunazkoe.newsaggregator.domain.article.entity.Article;
import com.lunazkoe.newsaggregator.global.common.dto.CursorPageResponse;

public interface ArticleRepositoryCustom {
    CursorPageResponse<Article> searchArticles(ArticleSearchCondition condition);
}
