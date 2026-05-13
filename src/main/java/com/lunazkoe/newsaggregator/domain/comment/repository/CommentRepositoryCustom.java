package com.lunazkoe.newsaggregator.domain.comment.repository;

import com.lunazkoe.newsaggregator.domain.comment.dto.request.SearchCommentCondition;
import com.lunazkoe.newsaggregator.domain.comment.entity.Comment;
import com.lunazkoe.newsaggregator.global.common.dto.CursorPageResponse;

public interface CommentRepositoryCustom {
    CursorPageResponse<Comment> searchComments(SearchCommentCondition condition);
}
