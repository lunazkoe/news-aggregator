package com.lunazkoe.newsaggregator.domain.activity.entity;

import com.lunazkoe.newsaggregator.domain.article.entity.Source;
import jakarta.persistence.Id;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Builder
@Document(collation = "user_activities")
public class UserActivity {

    @Id
    private UUID id; // User의 PK와 동일하게 매핑

    private String email;
    private String nickname;

    @CreatedDate
    private LocalDateTime createdAt;

    @Builder.Default
    private List<SubscriptionHistory> subscriptions = new ArrayList<>();

    @Builder.Default
    private List<CommentHistory> comments = new ArrayList<>();

    @Builder.Default
    private List<CommentLikeHistory> commentLikes = new ArrayList<>();

    @Builder.Default
    private List<ArticleViewHistory> articleViews = new ArrayList<>();

    public record SubscriptionHistory(
            UUID id,
            UUID interestId,
            String interestName,
            List<String> interestKeywords,
            int interestSubscriberCount,
            LocalDateTime createdAt
    ) {}

    public record CommentHistory(
            UUID id,
            UUID articleId,
            String articleTitle,
            String content,
            int likeCount,
            LocalDateTime createdAt
    ) {}

    public record CommentLikeHistory(
            UUID id,
            UUID commentId,
            UUID articleId,
            String articleTitle,
            UUID commentUserId,
            String commentUserNickname,
            String commentContent,
            int commentLikeCount,
            LocalDateTime createdAt
    ) {}

    public record ArticleViewHistory(
            UUID id,
            UUID articleId,
            Source source,
            String sourceUrl,
            String articleTitle,
            LocalDateTime articlePublishedDate,
            String articleSummary,
            int articleCommentCount,
            int articleViewCount,
            LocalDateTime createdAt
    ) {}
}
