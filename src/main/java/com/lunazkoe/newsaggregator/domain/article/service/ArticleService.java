package com.lunazkoe.newsaggregator.domain.article.service;

import com.lunazkoe.newsaggregator.domain.article.dto.request.ArticleSearchCondition;
import com.lunazkoe.newsaggregator.domain.article.dto.response.ArticleDto;
import com.lunazkoe.newsaggregator.domain.article.dto.response.ArticleViewDto;
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
import com.lunazkoe.newsaggregator.global.common.dto.CursorPageResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class ArticleService {

    private final ArticleRepository articleRepository;
    private final ArticleViewRepository articleViewRepository;
    private final UserRepository userRepository;

    @Transactional
    public ArticleViewDto recordArticleView(UUID articleId, UUID userId) {
        log.info("기사 조회 요청 처리 시작 - articleId: {}, userId: {}", articleId, userId);

        Article foundArticle = articleRepository.findById(articleId)
                .orElseThrow(() -> new ArticleException(ArticleErrorCode.ARTICLE_NOT_FOUND, Map.of("id", articleId)));

        Optional<ArticleView> existingView = articleViewRepository.findByArticleIdAndUserId(articleId, userId);

        // 1-1. 이미 조회한 이력이 있다면, 기존 정보를 그대로 반환 (멱등성 보장)
        if (existingView.isPresent()) {
            log.info("이미 조회한 기사입니다. 기존 이력을 반환합니다. - articleId: {}, userId: {}", articleId, userId);
            return ArticleViewDto.from(existingView.get());
        }

        User foundUser = userRepository.findById(userId)
                .orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND, Map.of("id", userId)));

        ArticleView newView = ArticleView.builder()
                .article(foundArticle)
                .user(foundUser)
                .build();

        articleViewRepository.save(newView);
        foundArticle.increaseViewCount();

        log.info("기사 조회 기록 완료 - viewId: {}", newView.getId());
        return ArticleViewDto.from(newView);
    }

    // 동시성은 현재 고려하지 않기
    @Transactional(readOnly = true)
    public ArticleDto getArticle(UUID articleId, UUID userId) {
        log.info("Fetching article details - Article ID: {}, User ID: {}", articleId, userId);

        Article foundArticle = foundArticle(articleId);

        boolean isAlreadyViewed = articleViewRepository.existsByArticleIdAndUserId(articleId, userId);

        // 조회를 시도 했으므로 본인 조회 여부는 true로 반환
        // - 이걸 굳이 왜 조회해야할까? 어차피 자기가 조회를 했으니깐 결국 true이긴한데?
        return ArticleDto.from(foundArticle, isAlreadyViewed);
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

    @Transactional(readOnly = true)
    public CursorPageResponse<ArticleDto> searchArticles(ArticleSearchCondition condition, UUID requestUserId) {
        CursorPageResponse<Article> pageResult = articleRepository.searchArticles(condition);
        List<Article> articles = pageResult.content();

        Set<UUID> viewedArticleIds;
        if (requestUserId != null && !articles.isEmpty()) {
            // 현재 페이지의 기사 ID 목록만 추출
            List<UUID> articleIds = articles.stream().map(Article::getId).toList();

            // IN 쿼리로 사용자가 읽은 기사 ID만 한 번에 가져와서 set에 담음
            viewedArticleIds = articleViewRepository.findViewedArticleIds(requestUserId, articleIds);
        } else {
            viewedArticleIds = new HashSet<>();
        }

        List<ArticleDto> dtos = articles.stream()
                .map(article -> new ArticleDto(
                        article.getId(),
                        article.getSource(),
                        article.getSourceUrl(),
                        article.getTitle(),
                        article.getPublishDate(),
                        article.getSummary(),
                        article.getCommentCount(),
                        article.getViewCount(),
                        viewedArticleIds.contains(article.getId())
                ))
                .toList();

        return new CursorPageResponse<>(
                dtos,
                pageResult.nextCursor(),
                pageResult.nextAfter(),
                pageResult.size(),
                pageResult.totalElements(),
                pageResult.hasNext()
        );
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
