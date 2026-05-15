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
import com.lunazkoe.newsaggregator.global.common.event.ArticleViewedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.context.ApplicationEventPublisher;
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
    private final ApplicationEventPublisher eventPublisher;

    /**
     * 기사 뷰 등록
     */
    @Transactional
    public ArticleViewDto recordArticleView(UUID articleId, UUID userId) {
        log.info("기사 조회 요청 처리 시작 - articleId: {}, userId: {}", articleId, userId);

        Article foundArticle = articleRepository.findById(articleId)
                .orElseThrow(() -> new ArticleException(ArticleErrorCode.ARTICLE_NOT_FOUND, Map.of("id", articleId)));

        Optional<ArticleView> existingView = articleViewRepository.findByArticleIdAndUserId(articleId, userId);

        // 이미 조회한 이력이 있다면, 기존 정보를 그대로 반환 (멱등성 보장)
        if (existingView.isPresent()) {
            log.info("이미 조회한 기사입니다. 기존 이력을 반환합니다. - articleId: {}, userId: {}", articleId, userId);
            return ArticleViewDto.from(existingView.get());
        }

        User foundUser = userRepository.findById(userId)
                .orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND, Map.of("id", userId)));

        // 기사 뷰 생성
        ArticleView newView = ArticleView.builder()
                .article(foundArticle)
                .user(foundUser)
                .build();

        articleViewRepository.save(newView);

        // 기사 뷰 증가
        foundArticle.increaseViewCount();

        log.info("기사 조회 기록 완료 - viewId: {}", newView.getId());

        eventPublisher.publishEvent(new ArticleViewedEvent(
                userId,
                newView.getId(),
                foundArticle.getId(),
                foundArticle.getSource(),
                foundArticle.getSourceUrl(),
                foundArticle.getTitle(),
                foundArticle.getPublishDate(),
                foundArticle.getSummary(),
                foundArticle.getCommentCount(),
                foundArticle.getViewCount(),
                foundArticle.getCreatedAt()
        ));

        return ArticleViewDto.from(newView);
    }

    /**
     * 뉴스 기사 목록 조회
     */
    @Transactional(readOnly = true)
    public CursorPageResponse<ArticleDto> searchArticles(ArticleSearchCondition condition, UUID requestUserId) {
        log.info("Searching Articles with condition: {}", condition);

        CursorPageResponse<Article> pageResponse = articleRepository.searchArticles(condition);
        List<Article> articles = pageResponse.content();

        Set<UUID> viewedArticleIds;
        if (requestUserId != null && !articles.isEmpty()) {
            // 현재 페이지의 기사 ID 목록만 추출
            List<UUID> articleIds = articles.stream().map(Article::getId).toList();

            // IN 쿼리로 사용자가 읽은 기사 ID만 한 번에 가져와서 set에 담음
            viewedArticleIds = articleViewRepository.findViewedArticleIds(requestUserId, articleIds);
        } else {
            viewedArticleIds = new HashSet<>();
        }

        // TODO: N + 1 문제 해결?
        // - 근데 일단 repository에 작성한 @Query가 문제가 있는지 파악해야할 듯
        List<ArticleDto> dtoList = articles.stream()
                .map(article -> ArticleDto.from(
                        article, viewedArticleIds.contains(article.getId())))
                .toList();

        return new CursorPageResponse<>(
                dtoList,
                pageResponse.nextCursor(),
                pageResponse.nextAfter(),
                pageResponse.size(),
                pageResponse.totalElements(),
                pageResponse.hasNext()
        );
    }

    /**
     * 뉴스 기사 단건 조회
     */
    // TODO: 동시성 고려 하기
    @Transactional(readOnly = true)
    public ArticleDto getArticle(UUID articleId, UUID userId) {
        log.info("Fetching article details - Article ID: {}, User ID: {}", articleId, userId);

        Article foundArticle = foundArticle(articleId);

        boolean isAlreadyViewed = articleViewRepository.existsByArticleIdAndUserId(articleId, userId);

        // TODO: 조회한다는 건 View를 했다는건데, 근데 View를 처리하는 다른 API가 있음
        // - 만약 API Spec이 잘못된 거면 그때 수정하기
        return ArticleDto.from(foundArticle, isAlreadyViewed);
    }

    /**
     * 뉴스 기사 논리 삭제
     */
    @Transactional
    public void softDeleteArticle(UUID articleId) {
        log.info("Soft deleting article - Article ID: {}", articleId);

        Article foundArticle = foundArticle(articleId);
        foundArticle.softDelete();

        // TODO: 일단 여기서는 삭제와 관련된 무언가를 진행하지 않음
        // - 논리 삭제가 되면 조회가 안되기 때문에 뭘 할 수가 없음 (위험이 없음)

        log.info("Successfully soft deleted article - Article ID: {}", articleId);
    }

    /**
     * 출처 목록 조회
     */
    // TODO: 출처 목록 조회는 그냥 컨트롤러 단에서 바로 반환하도록 구현
    // - 나중에 문제 있을시 수정

    /**
     * 뉴스 복구
     */
    // TODO: 뉴스 복구

    /**
     * 뉴스 기사 물리 삭제
     */
    // TODO: 물리 삭제 연관 관계에 대한 삭제 로직이 사실상 없는 것 이제 슬슬 해야겠지?
    // - 삭제 시 발생할 수 있는 문제들 파악 후 진행
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
