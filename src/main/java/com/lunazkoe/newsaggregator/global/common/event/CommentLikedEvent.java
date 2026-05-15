package com.lunazkoe.newsaggregator.global.common.event;

import java.time.LocalDateTime;
import java.util.UUID;

public record CommentLikedEvent(
        UUID userId, // 좋아요를 누른 사용자 ID
        UUID commentLikeId,
        UUID commentId,
        UUID articleId,
        String articleTitle,
        UUID commentUserId, // 원본 댓글 작성자 ID
        String commentUserNickname,
        String commentContent,
        int commentLikeCount,
        LocalDateTime createdAt,
        LocalDateTime commentCreatedAt
) {}