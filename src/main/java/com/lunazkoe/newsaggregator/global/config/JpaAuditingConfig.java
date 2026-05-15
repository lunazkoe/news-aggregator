package com.lunazkoe.newsaggregator.global.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.mongodb.config.EnableMongoAuditing;

@Configuration
@EnableJpaAuditing
@EnableMongoAuditing // MongoDB Document의 @CreatedDate
public class JpaAuditingConfig {
}
