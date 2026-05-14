package com.lunazkoe.newsaggregator.domain.comment.repository;

import com.lunazkoe.newsaggregator.domain.comment.entity.CommentLike;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface CommentLikeRepository extends JpaRepository<CommentLike, UUID> {

    // 좋아요 여부 확인
    boolean existsByUserIdAndCommentId(UUID userId, UUID commentId);

    // 좋아요 취소를 위한 단건 조회
    Optional<CommentLike> findByUserIdAndCommentId(UUID userId, UUID commentId);
}
