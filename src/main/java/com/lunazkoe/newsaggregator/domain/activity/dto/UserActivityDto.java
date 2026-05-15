package com.lunazkoe.newsaggregator.domain.activity.dto;

import com.lunazkoe.newsaggregator.domain.activity.entity.UserActivity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record UserActivityDto(
        UUID id,
        String email,
        String nickname,
        LocalDateTime createdAt,
        List<UserActivity.SubscriptionHistory> subscriptions,
        List<UserActivity.CommentHistory> comments,
        List<UserActivity.CommentLikeHistory> commentLikes,
        List<UserActivity.ArticleViewHistory> articleViews
) {
    public static UserActivityDto from(UserActivity activity) {
        return new UserActivityDto(
                activity.getId(),
                activity.getEmail(),
                activity.getNickname(),
                activity.getCreatedAt(),
                activity.getSubscriptions(),
                activity.getComments(),
                activity.getCommentLikes(),
                activity.getArticleViews()
        );
    }
}
