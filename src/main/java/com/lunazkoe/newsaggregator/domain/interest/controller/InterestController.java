package com.lunazkoe.newsaggregator.domain.interest.controller;

import com.lunazkoe.newsaggregator.domain.interest.service.InterestService;
import com.lunazkoe.newsaggregator.domain.interest.dto.request.InterestRegisterRequest;
import com.lunazkoe.newsaggregator.domain.interest.dto.request.InterestUpdateRequest;
import com.lunazkoe.newsaggregator.domain.interest.dto.response.InterestDto;
import io.swagger.v3.oas.annotations.Operation;
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
    @Operation(summary = "관심사 등록", description = "새로운 관심사를 등록합니다.")
    public InterestDto registerInterest(@Valid @RequestBody InterestRegisterRequest request) {
        log.info("Request to register new interest: {}", request.name());
        InterestDto response = interestService.register(request);
        return response;
    }

    @PatchMapping("/{interestId}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "관심사 정보 수정", description = "관심사 키워드를 수정합니다.")
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
    @Operation(summary = "관심사 물리 삭제", description = "관심사를 물리적으로 삭제합니다.")
    public void hardDeleteInterest(
            @PathVariable UUID interestId
    ) {
        log.info("Request to soft delete interest ID: {}", interestId);
        interestService.hardDelete(interestId);
    }
}
