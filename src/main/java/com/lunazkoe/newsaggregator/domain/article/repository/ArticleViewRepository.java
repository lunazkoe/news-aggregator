package com.lunazkoe.newsaggregator.domain.article.repository;

import com.lunazkoe.newsaggregator.domain.article.entity.ArticleView;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ArticleViewRepository extends JpaRepository<ArticleView, UUID> {
    // 특정 기사를 특정 유저가 이미 조회했는지 확인
    boolean existsByArticleIdAndUserId(UUID articleId, UUID userId);
}
