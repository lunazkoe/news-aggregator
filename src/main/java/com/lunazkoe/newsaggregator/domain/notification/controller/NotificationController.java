package com.lunazkoe.newsaggregator.domain.notification.controller;

import com.lunazkoe.newsaggregator.domain.notification.dto.request.SearchNotificationCondition;
import com.lunazkoe.newsaggregator.domain.notification.dto.response.NotificationDto;
import com.lunazkoe.newsaggregator.domain.notification.service.NotificationService;
import com.lunazkoe.newsaggregator.global.common.dto.CursorPageResponse;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

import static com.lunazkoe.newsaggregator.global.filter.MDCLoggingFilter.HEADER_USER_ID;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    @Operation(summary = "알림 목록 조회", description = "알림 목록을 조회합니다.")
    @GetMapping()
    @ResponseStatus(HttpStatus.OK)
    public CursorPageResponse<NotificationDto> searchNotificationsNotConfirmed(
            @ModelAttribute SearchNotificationCondition condition,
            @RequestHeader(HEADER_USER_ID) UUID userId
            ) {
        CursorPageResponse<NotificationDto> response = notificationService.searchNotificationsNotConfirmed(userId, condition);
        return response;
    }

    @Operation(summary = "전체 알림 확인", description = "전체 알림을 한번에 확인합니다.")
    @PatchMapping()
    @ResponseStatus(HttpStatus.OK)
    public void confirmAllNotification(@RequestHeader(HEADER_USER_ID) UUID requestUserId) {
        notificationService.confirmAllNotifications(requestUserId);
    }

    @Operation(summary = "알림 확인", description = "알림을 확인합니다.")
    @PatchMapping("/{notificationId}")
    @ResponseStatus(HttpStatus.OK)
    public void confirmNotification(
            @PathVariable UUID notificationId,
            @RequestHeader(HEADER_USER_ID) UUID requestUserId
    ) {
        notificationService.confirmNotification(notificationId, requestUserId);
    }
}
