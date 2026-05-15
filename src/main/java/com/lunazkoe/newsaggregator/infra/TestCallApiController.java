package com.lunazkoe.newsaggregator.infra;

import com.lunazkoe.newsaggregator.batch.collector.NewsCollectorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/call")
public class TestCallApiController {

    private final NewsCollectorService service;

    @GetMapping("/naver-news")
    public String triggerNaverNewsCollection() {
        service.collectNewsHourly();
        return "뉴스 수집 시작 DB 확인 ㄱ";
    }
}
