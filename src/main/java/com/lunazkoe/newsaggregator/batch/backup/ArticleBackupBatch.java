package com.lunazkoe.newsaggregator.batch.backup;

import com.lunazkoe.newsaggregator.domain.article.entity.Article;
import com.lunazkoe.newsaggregator.domain.article.repository.ArticleRepository;
import com.lunazkoe.newsaggregator.infra.s3.S3BackupUploader;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class ArticleBackupBatch {

    private final ArticleRepository articleRepository;
    private final S3BackupUploader s3BackupUploader;

    @Scheduled(cron = "0 0 0 * * *") // 매일 자정에 실행
    @Transactional(readOnly = true)
    public void executeDailyArticleBackup() {
        // HTTP Filter를 거치지 않으므로 수동으로 MDC Trace ID 주입
        MDC.put("REQUEST_ID", "BATCH-" + UUID.randomUUID().toString().substring(0, 8));

        try {
            LocalDate yesterday = LocalDate.now().minusDays(1);
            LocalDateTime startOfDay = yesterday.atStartOfDay();
            LocalDateTime endOfDay = yesterday.atTime(LocalTime.MAX);

            log.info("Starting daily article backup for date: {}", yesterday);

            // 어제 기준 기사 조회
            List<Article> articlesToBackup = articleRepository.findAllByCreatedAtBetween(startOfDay, endOfDay);

            if (articlesToBackup.isEmpty()) {
                log.info("백업할 기사가 없습니다. date: {}", yesterday);
                return;
            }

            // S3 저장 경로(Key) 생성 (예: backups/articles/2026/05/15/article_backup.json.gz)
            String datePath = yesterday.format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
            String s3Key = String.format("backups/articles/%s/article_backup.json.gz", datePath);

            // 압축 및 업로드 실행
            s3BackupUploader.uploadCompressedJson(s3Key, articlesToBackup);

            log.info("Daily article backup completed successfully. Total backed up: {}", articlesToBackup.size());
        } catch (Exception e) {
            log.error("Crtical Error during daily article backup", e);
        } finally {
            MDC.clear();
        }
    }
}
