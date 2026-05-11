package com.lunazkoe.newsaggregator.global.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.util.List;

@Configuration
@Profile({"dev", "local"})
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("News-Aggregator API 문서")
                        .description("News-Aggregator 프로젝트의 Swagger API 문서입니다.")
                        .version("0.0.1")
                )
                .servers(List.of( // 어느 서버 주소로 API 요청을 보낼 것인가
                        new Server().url("http://localhost:8080").description("Local Server")
                        // TODO: 운영 서버 주소
                ));
    }
}

// TODO: 나중에 회원가입/로그인 기능을 구현하고 JWT 토큰을 사용하게 된다면 components와 securityRequirement 설정을 추가
