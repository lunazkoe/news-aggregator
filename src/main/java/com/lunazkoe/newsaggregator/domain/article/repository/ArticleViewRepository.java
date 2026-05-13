package com.lunazkoe.newsaggregator.domain.article.repository;

import com.lunazkoe.newsaggregator.domain.article.entity.ArticleView;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public interface ArticleViewRepository extends JpaRepository<ArticleView, UUID> {
    // 특정 기사를 특정 유저가 이미 조회했는지 확인
    boolean existsByArticleIdAndUserId(UUID articleId, UUID userId);

    @Query("SELECT av.article.id FROM ArticleView av WHERE av.user.id = :userId AND av.article.id IN :articleIds")
    Set<UUID> findViewedArticleIds(UUID requestUserId, List<UUID> articleIds);
}
