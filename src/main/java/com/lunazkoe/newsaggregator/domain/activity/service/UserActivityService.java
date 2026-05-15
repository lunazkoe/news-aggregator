package com.lunazkoe.newsaggregator.domain.activity.service;

import com.lunazkoe.newsaggregator.domain.activity.dto.UserActivityDto;
import com.lunazkoe.newsaggregator.domain.activity.entity.UserActivity;
import com.lunazkoe.newsaggregator.domain.activity.exception.UserActivityErrorCode;
import com.lunazkoe.newsaggregator.domain.activity.exception.UserActivityException;
import com.lunazkoe.newsaggregator.domain.activity.repository.UserActivityRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserActivityService {

    private final UserActivityRepository userActivityRepository;

//    @Transactional
    // - MongoDB는 단일 Document 작업에 대해 원자성을 보장함
    public UserActivityDto getUserActivity(UUID userId) {
        log.info("MongoDB 사용자 활동 내역 조회 요청: userId={}", userId);

        UserActivity foundUserActivity = userActivityRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("사용자 활동 내역 Document를 찾을 수 없습니다. userId={}", userId);
                    return new UserActivityException(UserActivityErrorCode.USER_ACTIVITY_NOT_FOUND);
                });

        return UserActivityDto.from(foundUserActivity);
    }
}
