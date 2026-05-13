package com.lunazkoe.newsaggregator.domain.interest.repository;

import com.lunazkoe.newsaggregator.domain.interest.dto.request.InterestSearchCondition;
import com.lunazkoe.newsaggregator.domain.interest.entity.Interest;
import com.lunazkoe.newsaggregator.global.common.dto.CursorPageResponse;

public interface InterestRepositoryCustom {
    CursorPageResponse<Interest> searchInterests(InterestSearchCondition condition);
}
