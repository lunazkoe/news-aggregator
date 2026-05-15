package com.lunazkoe.newsaggregator.domain.activity.service;

import com.lunazkoe.newsaggregator.domain.activity.dto.UserActivityDto;
import com.lunazkoe.newsaggregator.domain.activity.entity.UserActivity;
import com.lunazkoe.newsaggregator.domain.activity.exception.UserActivityErrorCode;
import com.lunazkoe.newsaggregator.domain.activity.exception.UserActivityException;
import com.lunazkoe.newsaggregator.domain.activity.repository.UserActivityRepository;
import com.lunazkoe.newsaggregator.global.common.event.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserActivityService {

    private final UserActivityRepository userActivityRepository;
    private final MongoTemplate mongoTemplate;
    private static final int MAX_ACTIVITY_SIZE = 10;

//    @Transactional
    // - MongoDB는 단일 Document 작업에 대해 원자성을 보장함
    public UserActivityDto getUserActivity(UUID userId) {
        log.info("MongoDB 사용자 활동 내역 조회 요청: userId={}", userId);

        UserActivity foundUserActivity = userActivityRepository.findById(userId) // User의 PK랑 동일하게 매핑했으므로
                .orElseThrow(() -> {
                    log.warn("사용자 활동 내역 Document를 찾을 수 없습니다. userId={}", userId); // TODO: 근데 이거 좀 위험한데? => 없다는 뭔가 이상함...
                    return new UserActivityException(UserActivityErrorCode.USER_ACTIVITY_NOT_FOUND);
                });
        log.info("foundUserActivity:{}", foundUserActivity);
        return UserActivityDto.from(foundUserActivity);
    }

    //

    /**
     * 회원가입 후 빈 유저 활동 만들어두기
     */
    public void addUserRegisteredEventActivity(UserRegisteredEvent event) {
        log.info("[MongoDB Init] 신규 유저 활동 내역 문서 생성 로직 시작. User ID: {}", event.userId());
        Query query = Query.query(Criteria.where("_id").is(event.userId()));

        Update update = new Update()
                .set("email", event.email())
                .set("nickname", event.nickname())
                .setOnInsert("createdAt", LocalDateTime.now());

        mongoTemplate.upsert(query, update, UserActivity.class);
    }

    public void addSubscriptionActivity(SubscriptionEvent event) {
        log.info("MongoDB 구독 내역 업데이트 로직 실행 - userId: {}", event.userId());

        // 1. History 객체로 명시적 매핑
        UserActivity.SubscriptionHistory history = UserActivity.SubscriptionHistory.builder()
                .id(event.subscriptionId()) // Event 클래스의 필드명에 맞게 호출
                .interestId(event.interestId())
                .interestName(event.interestName())
                .interestKeywords(event.interestKeywords())
                .interestSubscriberCount(event.interestSubscriberCount())
                .createdAt(event.createdAt())
                .build();

        Update update = new Update().push("subscriptions")
                .atPosition(Update.Position.FIRST)  // 배열의 가장 앞에 추가 (최신순)
                .slice(MAX_ACTIVITY_SIZE)           // 최대 10개까지만 유지하고 오래된 것은 자동 삭제
                .each(history);// Event 레코드 객체 자체를 Document 내부 객체로 삽입
        upsertUserActivity(event.userId(), update);
    }

    public void addCommentActivity(CommentCreatedEvent event) {
        log.info("MongoDB 댓글 작성 내역 업데이트 로직 실행 - userId: {}", event.userId());

        UserActivity.CommentHistory history = UserActivity.CommentHistory.builder()
                .id(event.commentId())
                .articleId(event.articleId())
                .articleTitle(event.articleTitle())
                .userId(event.userId())
                .userNickname(event.userNickname())
                .content(event.content())
                .likeCount(event.likeCount())
                .createdAt(event.createdAt())
                .build();

        Update update = new Update().push("comments")
                .atPosition(Update.Position.FIRST)
                .slice(MAX_ACTIVITY_SIZE)
                .each(history);

        upsertUserActivity(event.userId(), update);
    }

    public void addCommentLikeActivity(CommentLikedEvent event) {
        log.info("MongoDB 댓글 좋아요 내역 업데이트 로직 실행 - userId: {}", event.userId());

        UserActivity.CommentLikeHistory history = UserActivity.CommentLikeHistory.builder()
                .id(event.commentLikeId()) // Event 클래스의 PK
                .commentId(event.commentId())
                .articleId(event.articleId())
                .articleTitle(event.articleTitle())
                .commentUserId(event.commentUserId())
                .commentUserNickname(event.commentUserNickname())
                .commentContent(event.commentContent())
                .commentLikeCount(event.commentLikeCount())
                .commentCreatedAt(event.commentCreatedAt())
                .createdAt(event.createdAt())
                .build();

        Update update = new Update().push("commentLikes")
                .atPosition(Update.Position.FIRST)
                .slice(MAX_ACTIVITY_SIZE)
                .each(history);

        upsertUserActivity(event.userId(), update);
    }

    public void addArticleViewActivity(ArticleViewedEvent event) {
        log.info("MongoDB 기사 조회 내역 업데이트 로직 실행 - userId: {}", event.userId());

        UserActivity.ArticleViewHistory history = UserActivity.ArticleViewHistory.builder()
                .id(event.articleViewId())
                .viewedBy(event.viewedBy())
                .articleId(event.articleId())
                .source(event.source().name())
                .sourceUrl(event.sourceUrl())
                .articleTitle(event.articleTitle())
                .articlePublishedDate(event.articlePublishedDate())
                .articleSummary(event.articleSummary())
                .articleCommentCount(event.articleCommentCount())
                .articleViewCount(event.articleViewCount())
                .createdAt(event.createdAt())
                .build();

        Update update = new Update().push("articleViews")
                .atPosition(Update.Position.FIRST)
                .slice(MAX_ACTIVITY_SIZE)
                .each(history);

        upsertUserActivity(event.userId(), update);
    }

    private void upsertUserActivity(UUID userId, Update update) {
        Query query = Query.query(Criteria.where("_id").is(userId));

        // 만약 문서가 없어서 새로 만들어야 할 경우 (이벤트 순서가 뒤집힌 경우)
        update.setOnInsert("createdAt", LocalDateTime.now());

        mongoTemplate.upsert(query, update, UserActivity.class);
    }
}
