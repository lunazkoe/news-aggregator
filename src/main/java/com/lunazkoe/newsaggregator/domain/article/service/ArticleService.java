package com.lunazkoe.newsaggregator.domain.article.service;

import com.lunazkoe.newsaggregator.domain.article.dto.response.ArticleDto;
import com.lunazkoe.newsaggregator.domain.article.entity.Article;
import com.lunazkoe.newsaggregator.domain.article.entity.ArticleView;
import com.lunazkoe.newsaggregator.domain.article.exception.ArticleErrorCode;
import com.lunazkoe.newsaggregator.domain.article.exception.ArticleException;
import com.lunazkoe.newsaggregator.domain.article.repository.ArticleRepository;
import com.lunazkoe.newsaggregator.domain.article.repository.ArticleViewRepository;
import com.lunazkoe.newsaggregator.domain.user.entity.User;
import com.lunazkoe.newsaggregator.domain.user.exception.UserErrorCode;
import com.lunazkoe.newsaggregator.domain.user.exception.UserException;
import com.lunazkoe.newsaggregator.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ArticleService {

    private final ArticleRepository articleRepository;
    private final ArticleViewRepository articleViewRepository;
    private final UserRepository userRepository;

    // 동시성은 현재 고려하지 않기
    @Transactional
    public ArticleDto getArticle(UUID articleId, UUID userId) {
        log.info("Fetching article details - Article ID: {}, User ID: {}", articleId, userId);

        Article foundArticle = foundArticle(articleId);

        User foundUser = foundUser(userId);

        boolean isAlreadyViewed = articleViewRepository.existsByArticleIdAndUserId(articleId, userId);

        if (!isAlreadyViewed) {
            // 처음 조회하는 경우 조회수 증가 및 이력 저장
            foundArticle.increaseViewCount();
            articleViewRepository.save(
                    ArticleView.builder()
                            .article(foundArticle)
                            .user(foundUser)
                            .build()
            );
            log.info("Increased view count for Article ID: {}", articleId);
        }

        // 조회를 시도 했으므로 본인 조회 여부는 true로 반환
        return ArticleDto.from(foundArticle, true);
    }

    @Transactional
    public void softDeleteArticle(UUID articleId) {
        log.info("Soft deleting article - Article ID: {}", articleId);

        Article foundArticle = foundArticle(articleId);
        foundArticle.softDelete();

        log.info("Successfully soft deleted article - Article ID: {}", articleId);
    }

    @Transactional
    public void hardDeleteArticle(UUID articleId) {
        log.info("Hard deleting article - Article ID: {}", articleId);

        Article foundArticle = foundArticle(articleId);
        articleRepository.delete(foundArticle);

        log.info("Successfully hard deleted article - Article ID: {}", articleId);
    }

    private  User foundUser(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND));
    }

    private Article foundArticle(UUID articleId) {
        return articleRepository.findById(articleId)
                .orElseThrow(() -> new ArticleException(ArticleErrorCode.ARTICLE_NOT_FOUND, Map.of("id", articleId)));
    }
}
