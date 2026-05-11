package com.lunazkoe.newsaggregator.global.config;

import com.lunazkoe.newsaggregator.domain.user.exception.UserErrorCode;
import com.lunazkoe.newsaggregator.domain.user.exception.UserException;
import com.lunazkoe.newsaggregator.global.common.dto.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/test")
public class TestController {

    @GetMapping("/success")
    public ApiResponse<Map<String, String>> successTest() {
        log.info("성공 응답 테스트 호출");
        return ApiResponse.success("성공 규격 테스트입니다.", Map.of("projectName", "MoNew"));
    }

    @GetMapping("/custom-error")
    public void customErrorTest() {
        log.info("커스텀 예외 테스트 호출");
        // 우리가 만든 비즈니스 예외(404 Not Found)를 의도적으로 발생시킴
        throw new UserException(UserErrorCode.USER_NOT_FOUND);
    }

    @GetMapping("/server-error")
    public void serverErrorTest() {
        log.info("서버 에러 테스트 호출");
        // 예상치 못한 시스템 예외(500 Internal Server Error)를 의도적으로 발생시킴
        throw new RuntimeException("의도적으로 발생시킨 런타임 에러입니다.");
    }
}
