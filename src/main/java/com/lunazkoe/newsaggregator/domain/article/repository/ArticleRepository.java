package com.lunazkoe.newsaggregator.domain.article.repository;

import com.lunazkoe.newsaggregator.domain.article.entity.Article;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public interface ArticleRepository extends JpaRepository<Article, UUID>, ArticleRepositoryCustom {
    @Query("SELECT a.sourceUrl FROM Article a WHERE a.sourceUrl IN :urls")
    List<String> findSourceUrlsBySourceUrlIn(@Param("urls") List<String> urls);

    List<Article> findAllByCreatedAtBetween(LocalDateTime start, LocalDateTime end);
}
