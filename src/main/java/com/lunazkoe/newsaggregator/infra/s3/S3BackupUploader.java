package com.lunazkoe.newsaggregator.infra.s3;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.ByteArrayOutputStream;
import java.util.zip.GZIPOutputStream;

@Slf4j
@Component
@RequiredArgsConstructor
public class S3BackupUploader {

    private final S3Client s3Client;
    private final ObjectMapper objectMapper;

    @Value("${cloud.aws.s3.bucket}")
    private String bucketName;

    /**
     * 데이터를 JSON 직렬화 -> GZIP 압축 후 S3에 업로드
     */
    public void uploadCompressedJson(String s3Key, Object data) {
        log.info("Starting compression and upload for S3 Key: {}", s3Key);

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             GZIPOutputStream gzipOut = new GZIPOutputStream(baos)) {
            // 객체를 JSON으로 변환하면서 동시에 GZIP 스트림으로 압축
            objectMapper.writeValue(gzipOut, data);
            gzipOut.finish(); // 압축 완료

            byte[] compressedData = baos.toByteArray();

            // S3에 업로드 요청 생성
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(s3Key)
                    .contentType("application/gzip")
                    .build();

            // S3에 파일 전송
            s3Client.putObject(putObjectRequest, RequestBody.empty().fromBytes(compressedData));

            log.info("Successfully uploaded compressed backup to S3. Key: {}, Size: {} bytes", s3Key, compressedData.length);
        } catch (Exception e) {
            log.error("Failed to compress and upload backup to S3. Key: {}", s3Key, e);
            // 필요에 따라 글로벌 예외로 던져서 알림을 발생시킬 수 있습니다.
            throw new RuntimeException("S3 Backup Upload Failed", e);
        }
    }
}
