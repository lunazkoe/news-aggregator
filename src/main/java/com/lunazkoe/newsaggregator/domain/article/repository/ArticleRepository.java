package com.lunazkoe.newsaggregator.domain.article.repository;

import com.lunazkoe.newsaggregator.domain.article.entity.Article;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ArticleRepository extends JpaRepository<Article, UUID>, ArticleRepositoryCustom {
}
