package com.lunazkoe.newsaggregator;

import com.lunazkoe.newsaggregator.domain.user.entity.User;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

@Slf4j
public class TestUtils {

    @Test
    void test() {
        User user = User.builder()
                .email("test@email.com")
                .nickname("user")
                .password("pass")
                .build();

        log.info("user.getDeletedAt() = {}", user.getDeletedAt());
        log.info("user.isDeleted() = {}", user.isDeleted());
    }
}
