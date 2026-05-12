package com.lunazkoe.newsaggregator.domain.interest.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

@Schema(description = "관심사 정보")
public record InterestRegisterRequest(
        @Schema(description = "관심사 이름")
        @NotBlank(message = "관심사 이름은 필수입니다.")
        @Size(min = 1, max = 50, message = "관심사 이름은 1자 이상 50자 이하로 입력해주세요.")
        String name,

        @Schema(description = "관련 키워드 목록")
        @NotNull(message = "키워드 목록은 필수입니다.")
        @Size(min = 1, max = 10, message = "키워드는 1개 이상 10개 이하로 등록 가능합니다.")
        List<String> keywords
) {}
