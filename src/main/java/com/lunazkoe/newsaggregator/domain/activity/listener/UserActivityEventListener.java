package com.lunazkoe.newsaggregator.domain.activity.listener;

import com.lunazkoe.newsaggregator.domain.activity.entity.UserActivity;
import com.lunazkoe.newsaggregator.domain.activity.service.UserActivityService;
import com.lunazkoe.newsaggregator.global.common.event.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.time.LocalDateTime;
import java.util.ArrayList;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserActivityEventListener {

    private final UserActivityService userActivityService;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleSubscriptionEvent(SubscriptionEvent event) {
        log.info("비동기 이벤트 수신 [구독 동기화] - userId: {}, interestId: {}", event.userId(), event.interestId());
        userActivityService.addSubscriptionActivity(event);
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleCommentCreatedEvent(CommentCreatedEvent event) {
        log.info("비동기 이벤트 수신 [댓글 작성 동기화] - userId: {}, commentId: {}", event.userId(), event.commentId());
        userActivityService.addCommentActivity(event);
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleCommentLikedEvent(CommentLikedEvent event) {
        log.info("비동기 이벤트 수신 [댓글 좋아요 동기화] - userId: {}, commentId: {}", event.userId(), event.commentId());
        userActivityService.addCommentLikeActivity(event);
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleArticleViewedEvent(ArticleViewedEvent event) {
        log.info("비동기 이벤트 수신 [기사 조회 동기화] - userId: {}, articleId: {}", event.userId(), event.articleId());
        userActivityService.addArticleViewActivity(event);
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleUserRegisteredEvent(UserRegisteredEvent event) {
        log.info("비동기 이벤트 수신 [회원 가입 확인 후 MongoDB 활동 내역서 발급] userId: {}", event.userId());
        userActivityService.addUserRegisteredEventActivity(event);
    }
}
