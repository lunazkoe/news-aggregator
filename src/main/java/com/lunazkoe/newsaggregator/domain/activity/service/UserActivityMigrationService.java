package com.lunazkoe.newsaggregator.domain.activity.service;

import com.lunazkoe.newsaggregator.domain.activity.entity.UserActivity;
import com.lunazkoe.newsaggregator.domain.activity.repository.UserActivityRepository;
import com.lunazkoe.newsaggregator.domain.article.repository.ArticleViewRepository;
import com.lunazkoe.newsaggregator.domain.comment.repository.CommentLikeRepository;
import com.lunazkoe.newsaggregator.domain.comment.repository.CommentRepository;
import com.lunazkoe.newsaggregator.domain.interest.repository.SubscriptionRepository;
import com.lunazkoe.newsaggregator.domain.user.entity.User;
import com.lunazkoe.newsaggregator.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

// TODO: 성능 문제 무조건 터진다
// - User가 너무 많을 때 OOM 등

@Slf4j
@Service
@RequiredArgsConstructor
public class UserActivityMigrationService {

    private final UserRepository userRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final CommentRepository commentRepository;
    private final CommentLikeRepository commentLikeRepository;
    private final ArticleViewRepository articleViewRepository;
    private final UserActivityRepository userActivityRepository;

    /**
     * RDBMS의 모든 유저 데이터를 읽어서 MongoDB UserActivity Document로 변환
     */
    @Transactional(readOnly = true)
    public void migrateAllUsers() {
        log.info("RDBMS -> MongoDB 데이터 마이그레이션 시작");

        List<User> users = userRepository.findAll();
        long count = 0;

        for (User user : users) {
            if (userActivityRepository.existsById(user.getId())) {
                log.debug("User {} 이미 MongoDB에 존재함. 스킵.", user.getId());
                continue;
            }

            // 1. 구독 정보 조회 (최신 10건) - 기존 Repository 메서드 활용 또는 추가 필요
            List<UserActivity.SubscriptionHistory> subscriptions = subscriptionRepository.findTop10ByUserIdOrderByCreatedAtDesc(user.getId())
                    .stream()
                    .map(sub -> new UserActivity.SubscriptionHistory(
                            sub.getId(),
                            sub.getInterest().getId(),
                            sub.getInterest().getName(),
                            sub.getInterest().getKeywords(),
                            sub.getInterest().getSubscriberCount(),
                            sub.getCreatedAt()
                    )).collect(Collectors.toList());

            // 2. 작성 댓글 조회 (최신 10건)
            List<UserActivity.CommentHistory> comments = commentRepository.findTop10ByUserIdOrderByCreatedAtDesc(user.getId())
                    .stream()
                    .map(comment -> new UserActivity.CommentHistory(
                            comment.getId(),
                            comment.getArticle().getId(),
                            comment.getArticle().getTitle(),
                            comment.getContent(),
                            comment.getLikeCount(),
                            comment.getCreatedAt()
                    )).collect(Collectors.toList());

            // 3. 좋아요 누른 댓글 조회 (최신 10건)
            List<UserActivity.CommentLikeHistory> commentLikes = commentLikeRepository.findTop10ByUserIdOrderByCreatedAtDesc(user.getId())
                    .stream()
                    .map(like -> new UserActivity.CommentLikeHistory(
                            like.getId(),
                            like.getComment().getId(),
                            like.getComment().getArticle().getId(),
                            like.getComment().getArticle().getTitle(),
                            like.getComment().getUser().getId(),
                            like.getComment().getUser().getNickname(),
                            like.getComment().getContent(),
                            like.getComment().getLikeCount(),
                            like.getCreatedAt()
                    )).collect(Collectors.toList());

            // 4. 최근 본 기사 조회 (최신 10건)
            List<UserActivity.ArticleViewHistory> articleViews = articleViewRepository.findTop10ByUserIdOrderByCreatedAtDesc(user.getId())
                    .stream()
                    .map(view -> new UserActivity.ArticleViewHistory(
                            view.getId(),
                            view.getArticle().getId(),
                            view.getArticle().getSource(),
                            view.getArticle().getSourceUrl(),
                            view.getArticle().getTitle(),
                            view.getArticle().getPublishDate(),
                            view.getArticle().getSummary(),
                            view.getArticle().getCommentCount(),
                            view.getArticle().getViewCount(),
                            view.getCreatedAt()
                    )).collect(Collectors.toList());


            // UserActivity Document 생성
            UserActivity userActivity = UserActivity.builder()
                    .id(user.getId())
                    .email(user.getEmail())
                    .nickname(user.getNickname())
                    .createdAt(user.getCreatedAt())
                    .subscriptions(subscriptions)
                    .comments(comments)
                    .commentLikes(commentLikes)
                    .articleViews(articleViews)
                    .build();

            // MongoDB에 저장
            userActivityRepository.save(userActivity);
            count++;
        }
    }
}
