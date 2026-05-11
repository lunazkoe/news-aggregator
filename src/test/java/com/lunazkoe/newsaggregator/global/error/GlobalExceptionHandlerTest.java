package com.lunazkoe.newsaggregator.global.error;

import com.lunazkoe.newsaggregator.domain.user.exception.UserErrorCode;
import com.lunazkoe.newsaggregator.global.config.TestController;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TestController.class)
class GlobalExceptionHandlerTest {

    @Autowired
    MockMvc mockMvc;

    @Test
    @DisplayName("커스텀 비즈니스 예외 발생 시 ErrorResponse 규격으로 응답해야 한다.")
    void customErrorTest() throws Exception {
        mockMvc.perform(get("/api/test/custom-error"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(UserErrorCode.USER_NOT_FOUND.getCode()))
                .andExpect(jsonPath("$.message").value(UserErrorCode.USER_NOT_FOUND.getMessage()));
    }

    @Test
    @DisplayName("서버 내부 에러 발생 시 500 에러 규격으로 응답해야 한다.")
    void serverErrorTest() throws Exception {
        // RuntimeException이 발생하도록 설계된 엔드포인트 호출
        mockMvc.perform(get("/api/test/server-error"))
                .andExpect(status().isInternalServerError()) // HTTP 상태 코드가 500인지 검증
                .andExpect(jsonPath("$.code").value(GlobalErrorCode.INTERNAL_SERVER_ERROR.getCode()))
                .andExpect(jsonPath("$.message").value(GlobalErrorCode.INTERNAL_SERVER_ERROR.getMessage()));
    }
}