package com.lunazkoe.newsaggregator.domain.comment.repository;

import com.lunazkoe.newsaggregator.domain.comment.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CommentRepository extends JpaRepository<Comment, UUID>, CommentRepositoryCustom {
    List<Comment> findTop10ByUserIdOrderByCreatedAtDesc(UUID userId);

    // Fetch Join을 사용하여 Comment, Article, User를 한 번의 쿼리로 조회
    @Query("SELECT c FROM Comment c JOIN FETCH c.article JOIN FETCH c.user WHERE c.id = :id AND c.isDeleted = false")
    Optional<Comment> findByIdWithArticleAndUser(@Param("id") UUID id);
}
