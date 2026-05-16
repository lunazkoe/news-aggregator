package com.lunazkoe.newsaggregator.infra.s3;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lunazkoe.newsaggregator.domain.article.entity.Article;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;

import java.util.Collections;
import java.util.List;
import java.util.zip.GZIPInputStream;

/**
 * 복구 로직 주의사항
 * - 그냥 밀어버리면 이미 DB에 있는 데이터일 경우 문제가 발생할 수 있음
 * - 차집합을 해서 없는 데이터만 가져와서 밀어버리기
 */

@Slf4j
@Component
@RequiredArgsConstructor
public class S3BackupDownloader {

    private final S3Client s3Client;
    private final ObjectMapper objectMapper;

    @Value("${cloud.aws.s3.bucket}")
    private String bucketName;

    /**
     * S3에서 GZIP 압축된 JSON 파일을 다운로드하여 Article 객체 리스트로 반환
     */
    public List<Article> downloadAndDecompressBackup(String s3Key) {
        log.info("[S3BackupDownloader] 다운로드 시작 S3 Key: {}", s3Key);

        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(s3Key)
                .build();

        try {
            // S3에서 파일 스트림 받아오기
            ResponseInputStream<GetObjectResponse> s3Object = s3Client.getObject(getObjectRequest);

            // GZIP 압축 해제 및 JSON 역직렬화 (스트림 파이프라인)
            try (GZIPInputStream gzipIn = new GZIPInputStream(s3Object)) {
                List<Article> backedUpArticles = objectMapper.readValue(gzipIn, new TypeReference<List<Article>>() {});
                log.info("[S3BackupDownloader] 성공적으로 다운로드 및 역직렬화 완료. Key: {}, Data Count: {}", s3Key, backedUpArticles.size());
                return backedUpArticles;
            }

        } catch (NoSuchKeyException e) {
            // 해당 날짜의 백업 파일이 없는 경우 로직을 중단하지 않고 빈 리스트 반환
            log.warn("[S3BackupDownloader] 해당 S3 Key의 백업 파일이 존재하지 않습니다. Key: {}", s3Key);
            return Collections.emptyList();
        } catch (Exception e) {
            log.error("[S3BackupDownloader] S3 다운로드 및 압축 해제 중 치명적인 오류 발생. Key: {}", s3Key, e);
            throw new RuntimeException("S3 백업 다운로드 실패", e);
        }
    }
}
