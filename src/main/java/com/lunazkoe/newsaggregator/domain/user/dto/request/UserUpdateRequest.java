package com.lunazkoe.newsaggregator.domain.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "수정할 사용자 정보")
public record UserUpdateRequest(

        @Schema(description = "수정 닉네임", minLength = 1, maxLength = 20)
        @NotBlank(message = "변경할 닉네임을 입력해 주세요.")
        @Size(min = 1, max = 20, message = "닉네임은 1자 이상 20자 이하여야 합니다.")
        String nickname
) {
}
