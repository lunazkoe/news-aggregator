package com.lunazkoe.newsaggregator.domain.comment.repository;

import com.lunazkoe.newsaggregator.domain.comment.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface CommentRepository extends JpaRepository<Comment, UUID>, CommentRepositoryCustom {
    List<Comment> findTop10ByUserIdOrderByCreatedAtDesc(UUID userId);
}
