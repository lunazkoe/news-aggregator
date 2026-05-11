package com.lunazkoe.newsaggregator.global.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
// - Json으로 직렬화 시 아래 status, message, data 중 null이 있으면 해당 필드 자체를 아에 빼버린다는 의미
public record ApiResponse<T>(
        String status,
        String message,
        T data
) {
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>("SUCCESS", "요청이 성공적으로 처리되었습니다.", data);
    }

    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>("SUCCESS", message, data);
    }
}
