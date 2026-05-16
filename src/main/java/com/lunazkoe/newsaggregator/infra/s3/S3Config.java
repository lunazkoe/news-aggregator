package com.lunazkoe.newsaggregator.infra.s3;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;


// AWS S3 파일 저장소와 통신하기 위한 기본 설정
// - 파일을 업로드하거나 다운로드하려면 내가 누구인지 어느 지역의 S3를 쓸 것인지 알려주는 객체가 필요함
// - 이게 S3Client
@Configuration
public class S3Config {

    @Value("${cloud.aws.credentials.access-key}")
    private String accessKey;

    @Value("${cloud.aws.credentials.secret-key}")
    private String secretKey;

    @Value("${cloud.aws.region.static}")
    private String region;

    @Bean
    public S3Client s3Client() {
        AwsBasicCredentials credentials = AwsBasicCredentials.create(
                accessKey,
                secretKey
        );
        return S3Client.builder()
                .region((Region.of(region)))
                .credentialsProvider(StaticCredentialsProvider.create(credentials))
                .build();
    }
}
