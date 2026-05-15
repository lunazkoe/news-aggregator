package com.lunazkoe.newsaggregator.domain.activity.controller;

import com.lunazkoe.newsaggregator.domain.activity.dto.UserActivityDto;
import com.lunazkoe.newsaggregator.domain.activity.service.UserActivityService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/user-activities")
public class UserActivityController {

    private final UserActivityService userActivityService;

    @Operation(summary = "사용자 활동 내역 조회", description = "사용자 ID로 활동 내역을 조회합니다.")
    @GetMapping("/{userId}")
    @ResponseStatus(HttpStatus.OK)
    public UserActivityDto getUserActivities(@PathVariable UUID userId) {
        log.info("사용자 활동 내역 조회 API 호출: userId={}", userId);
        UserActivityDto response = userActivityService.getUserActivity(userId);
        return response;
    }
}
