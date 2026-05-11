package com.lunazkoe.newsaggregator.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
public class TestController {

    @GetMapping("/test")
    public String getTest() {
        log.info("GET /test called");
        return "Hello Test";
    }
}
