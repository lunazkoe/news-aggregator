package com.lunazkoe.newsaggregator.domain.interest.controller;

import com.lunazkoe.newsaggregator.domain.interest.service.InterestService;
import com.lunazkoe.newsaggregator.domain.interest.dto.request.InterestRegisterRequest;
import com.lunazkoe.newsaggregator.domain.interest.dto.request.InterestUpdateRequest;
import com.lunazkoe.newsaggregator.domain.interest.dto.response.InterestDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/interests")
@RequiredArgsConstructor
public class InterestController {

    private final InterestService interestService;

    @PostMapping()
    @ResponseStatus(HttpStatus.CREATED)
    public InterestDto registerInterest(@Valid @RequestBody InterestRegisterRequest request) {
        log.info("Request to register new interest: {}", request.name());
        InterestDto response = interestService.register(request);
        return response;
    }

    @PatchMapping("/{interestId}")
    @ResponseStatus(HttpStatus.OK)
    public InterestDto updateInterest(
            @PathVariable("interestId")UUID interestId,
            @Valid @RequestBody InterestUpdateRequest request
    ) {
        log.info("Request to update interest ID: {}", interestId);
        InterestDto response = interestService.update(interestId, request);
        return response;
    }

    @DeleteMapping("/{interestId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void hardDelete(
            @PathVariable UUID interestId
    ) {
        log.info("Request to soft delete interest ID: {}", interestId);
        interestService.hardDelete(interestId);
    }

    // - 관심사 목록 조회
    // - 관심사 구독
    // - 관심사 구독 취소
}
