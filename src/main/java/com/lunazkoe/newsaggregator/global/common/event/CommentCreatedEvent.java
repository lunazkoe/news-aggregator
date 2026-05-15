package com.lunazkoe.newsaggregator.global.common.event;

import java.time.LocalDateTime;
import java.util.UUID;

public record CommentCreatedEvent(
        UUID userId, // 작성자 ID
        UUID commentId,
        UUID articleId,
        String articleTitle,
        String userNickname,
        String content,
        int likeCount,
        LocalDateTime createdAt
) {}