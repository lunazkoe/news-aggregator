package com.lunazkoe.newsaggregator.domain.interest.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

@Schema(description = "수정할 관심사 정보")
public record InterestUpdateRequest(
        @Schema(description = "수정 키워드 목록")
        @NotNull(message = "키워드 목록은 필수입니다.")
        @Size(min = 1, max = 10, message = "키워드는 1개 이상 10개 이하로 등록 가능합니다.")
        List<String> keywords
) {}
