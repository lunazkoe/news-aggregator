package com.lunazkoe.newsaggregator.domain.comment.dto.response;

import com.lunazkoe.newsaggregator.domain.comment.entity.Comment;

import java.time.LocalDateTime;
import java.util.UUID;

public record CommentDto(
        UUID id,
        UUID articleId,
        UUID userId,
        String userNickname,
        String content,
        Integer likeCount,
        Boolean likedByMe,
        LocalDateTime createdAt
) {
    public static CommentDto from(Comment comment, String userNickname, boolean likedByMe) {
        return new CommentDto(
                comment.getId(),
                comment.getArticleId(),
                comment.getUserId(),
                userNickname,
                comment.getContent(),
                comment.getLikeCount(),
                likedByMe,
                comment.getCreatedAt()
        );
    }
}
