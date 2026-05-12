package com.lunazkoe.newsaggregator.domain.user.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "로그인 정보")
public record UserLoginRequest(

        @Schema(description = "로그인 이메일")
        @NotBlank(message = "이메일을 입력해 주세요.")
        String email,

        @Schema(description = "로그인 비밀번호")
        @NotBlank(message = "비밀번호를 입력해 주세요.")
        String password
) {
}
