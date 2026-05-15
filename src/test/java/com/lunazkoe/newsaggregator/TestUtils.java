package com.lunazkoe.newsaggregator;

import com.lunazkoe.newsaggregator.domain.activity.entity.UserActivity;
import com.lunazkoe.newsaggregator.domain.user.entity.User;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

@Slf4j
public class TestUtils {

    @Test
    void test() {
        UserActivity userActivity = UserActivity.builder()
                .email("test@gmail.com")
                .nickname("test")
                .build();

        log.info("userActivity={}", userActivity);
    }
}
