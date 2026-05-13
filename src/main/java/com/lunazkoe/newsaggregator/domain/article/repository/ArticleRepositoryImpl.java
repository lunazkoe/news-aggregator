package com.lunazkoe.newsaggregator.domain.article.repository;

import com.lunazkoe.newsaggregator.domain.article.entity.Article;
import com.lunazkoe.newsaggregator.domain.article.entity.Source;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class ArticleRepositoryImpl implements ArticleRepositoryCustom{

    @Override
    public List<Article> searchArticles(
            String keyword, UUID interestId, List<Source> sourceIn, LocalDateTime publishDateFrom, LocalDateTime publishDateTo, String orderBy, String direction, String cursor, LocalDateTime after, int limit) {

        // TODO: 동적 쿼리 및 커서 페이징 로직 구현 예정

        return List.of();
    }
}
