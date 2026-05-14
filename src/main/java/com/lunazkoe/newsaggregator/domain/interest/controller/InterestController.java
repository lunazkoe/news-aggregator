package com.lunazkoe.newsaggregator.domain.interest.controller;

import com.lunazkoe.newsaggregator.domain.interest.dto.request.InterestSearchCondition;
import com.lunazkoe.newsaggregator.domain.interest.dto.response.SubscriptionDto;
import com.lunazkoe.newsaggregator.domain.interest.service.InterestService;
import com.lunazkoe.newsaggregator.domain.interest.dto.request.InterestRegisterRequest;
import com.lunazkoe.newsaggregator.domain.interest.dto.request.InterestUpdateRequest;
import com.lunazkoe.newsaggregator.domain.interest.dto.response.InterestDto;
import com.lunazkoe.newsaggregator.global.common.dto.CursorPageResponse;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

import static com.lunazkoe.newsaggregator.global.filter.MDCLoggingFilter.*;

@Slf4j
@RestController
@RequestMapping("/api/interests")
@RequiredArgsConstructor
public class InterestController {

    private final InterestService interestService;

    @Operation(summary = "관심사 목록 조회", description = "조건에 맞는 관심사 목록을 조회합니다.")
    @GetMapping()
    @ResponseStatus(HttpStatus.OK)
    public CursorPageResponse<InterestDto> searchInterests(
            @ModelAttribute InterestSearchCondition condition,
            @RequestHeader(HEADER_USER_ID) UUID requestUserId
    ) {
        log.info("Request to search interests. keyword: {}, orderBy: {}", condition.keyword(), condition.orderBy());
        CursorPageResponse<InterestDto> response = interestService.searchInterests(condition, requestUserId);
        return response;
    }

    @Operation(summary = "관심사 등록", description = "새로운 관심사를 등록합니다.")
    @PostMapping()
    @ResponseStatus(HttpStatus.CREATED)
    public InterestDto registerInterest(@Valid @RequestBody InterestRegisterRequest request) {
        log.info("Request to register new interest: {}", request.name());
        InterestDto response = interestService.register(request);
        return response;
    }

    @Operation(summary = "관심사 구독", description = "관심사를 구독합니다.")
    @PostMapping("/{interestId}/subscriptions")
    @ResponseStatus(HttpStatus.OK)
    public SubscriptionDto subscription(@PathVariable UUID interestId, @RequestHeader(HEADER_USER_ID) UUID requestUserId) {
        SubscriptionDto response = interestService.subscribe(interestId, requestUserId);
        return response;
    }

    @Operation(summary = "관심사 구독 취소", description = "관심사를 구독 취소합니다.")
    @DeleteMapping("/{interestId}/subscriptions")
    @ResponseStatus(HttpStatus.OK)
    public void cancelSubscription(
            @PathVariable UUID interestId,
            @RequestHeader(HEADER_USER_ID) UUID requestUserId
    ) {
        interestService.cancelSubscription(interestId, requestUserId);
    }

    @Operation(summary = "관심사 물리 삭제", description = "관심사를 물리적으로 삭제합니다.")
    @DeleteMapping("/{interestId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void hardDeleteInterest(
            @PathVariable UUID interestId
    ) {
        log.info("Request to soft delete interest ID: {}", interestId);
        interestService.hardDelete(interestId);
    }

    @Operation(summary = "관심사 정보 수정", description = "관심사 키워드를 수정합니다.")
    @PatchMapping("/{interestId}")
    @ResponseStatus(HttpStatus.OK)
    public InterestDto updateInterest(
            @PathVariable("interestId")UUID interestId,
            @Valid @RequestBody InterestUpdateRequest request,
            @RequestHeader(HEADER_USER_ID) UUID requestUserId
    ) {
        log.info("Request to update interest ID: {}", interestId);
        InterestDto response = interestService.update(interestId, request, requestUserId);
        return response;
    }
}
