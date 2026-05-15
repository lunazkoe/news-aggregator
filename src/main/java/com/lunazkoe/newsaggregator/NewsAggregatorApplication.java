package com.lunazkoe.newsaggregator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync // 비동기 처리 활성화를 해주기 (안해도 되긴 하는데)
// - @EventListener를 사용할 경우 이벤트를 발행하는 시점에 실행
// - 해당 로직에 예외가 발생해도 이벤트를 발행하는 문제가 생길 수 있음
// - @TransactionalEventListener로 리스너를 등록하게 되면 해당 트랜잭션이 Commit된 이후 리스너가 동작함
// - 이벤트 핸들러는 기본적으로 동기적으로 실행됨
// - 비동기적으로 이벤트를 수신하려면 이벤트 리스너에 @Async를 지정하고 최상위에 @EnableAsync를 붙여줘야함
// - @Async를 하면 기존 스레드와 분리되고 자연스럽게 트랜잭션도 분리됨
@SpringBootApplication
public class NewsAggregatorApplication {

    public static void main(String[] args) {
        SpringApplication.run(NewsAggregatorApplication.class, args);
    }

}
