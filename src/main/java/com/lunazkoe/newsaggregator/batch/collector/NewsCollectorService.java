package com.lunazkoe.newsaggregator.batch.collector;

import com.lunazkoe.newsaggregator.domain.article.entity.Article;
import com.lunazkoe.newsaggregator.domain.article.entity.Source;
import com.lunazkoe.newsaggregator.domain.article.repository.ArticleRepository;
import com.lunazkoe.newsaggregator.domain.interest.repository.InterestRepository;
import com.lunazkoe.newsaggregator.infra.externalapi.naver.client.NaverNewsClient;
import com.lunazkoe.newsaggregator.infra.externalapi.naver.dto.NaverNewsItem;
import com.lunazkoe.newsaggregator.infra.externalapi.naver.dto.NaverNewsResponse;
import com.lunazkoe.newsaggregator.infra.externalapi.rss.RssNewsParser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class NewsCollectorService {

    private final NaverNewsClient naverNewsClient;
    private final RssNewsParser rssNewsParser;
    private final InterestRepository interestRepository;
    private final ArticleRepository articleRepository;

    // 한국경제 RSS URL (예시임)
    private static final String HANKYUNG_RSS_URL = "https://rss.hankyung.com/feed/it.xml";

    // 네이버 날짜 포맷 파싱용
    private static final DateTimeFormatter NAVER_DATE_FORMATTER = DateTimeFormatter.RFC_1123_DATE_TIME;

    @Value("${external-api.naver.client-id}")
    private String naverClientId;

    @Value("${external-api.naver.client-secret}")
    private String naverClientSecret;

    @Scheduled(cron = "0 0 * * * *")
    // - 여기에 Trnasational을 해야할까? => DB 커넥션 풀 점유?
    public void collectNewsHourly() {
        MDC.put("traceId", "BATCH-" + UUID.randomUUID().toString().substring(0, 8));
        log.info("[뉴스 수집 배치 시작] 시간당 뉴스 수집을 시작합니다.");

        try {
            // 등록된 모든 관심사 키워드 조회 (다음 스텝 구현)
            Set<String> allKeywords = getAllInterestKeywords();
            if (allKeywords.isEmpty()) {
                log.info("[뉴스 수집 배치] 등록된 관심사 키워드가 없어 수집을 생략합니다.");
                return;
            }

            ArrayList<Article> newsArticles = new ArrayList<>();

            // 키워드 기반으로 네이버 뉴스 API 수집
            newsArticles.addAll(collectFromNaver(allKeywords));

            // 고정된 RSS 피드 파싱 및 저장 ((한국경제)
//            newsArticles.addAll(collectFromRss(HANKYUNG_RSS_URL, Source.HANKYUNG));

            // DB 저장 전 URL 기준 중복 필터링 (이미 DB에 있는 기사 제외)
            List<Article> articlesToSave = filterExistingArticles(newsArticles);

            // 최종 적재
            if (!articlesToSave.isEmpty()) {
                articleRepository.saveAll(articlesToSave);
                log.info("[뉴스 수집 배치 완료] 새로운 뉴스 {}건이 DB에 적재되었습니다.", articlesToSave.size());
            } else {
                log.info("[뉴스 수집 배치 완료] 새로 적재할 뉴스 기사가 없습니다.");
            }

            log.info("[뉴스 수집 배치 완료] 모든 데이터 수집 및 적재가 정상 종료되었습니다.");
        } catch (Exception e) {
            log.error("[뉴스 수집 배치 실패] 원인: {}", e.getMessage(), e);
        } finally {
            MDC.clear();
        }
    }

    private Set<String> getAllInterestKeywords() {
        return interestRepository.findAll().stream()
                .filter(interest -> !interest.getIsDeleted())
                .flatMap(interest -> interest.getKeywords().stream())
                .collect(Collectors.toSet());
    }

    private List<Article> collectFromNaver(Set<String> keywords) {
        List<Article> naverArticles = new ArrayList<>();

        for (String keyword : keywords) {
            try {
                // 각 키워드별로 10개씩 최신순으로 가져옴
                NaverNewsResponse response = naverNewsClient.searchNews(
                        naverClientId, naverClientSecret, keyword, 10, 1, "date");

                if (response != null && response.items() != null) {
                    for (NaverNewsItem item : response.items()) {
                        naverArticles.add(Article.builder()
                                .source(Source.NAVER)
                                .sourceUrl(item.originallink()) // 원본 링크를 고유 식별자로 사용
                                .title(cleanHtmlTags(item.title()))
                                .summary(cleanHtmlTags(item.description()))
                                .publishDate(parseNaverDate(item.pubDate()))
                                .build());
                    }
                }
            } catch (Exception e) {
                log.warn("[Naver API 수집 실패] 키워드: {}, 에러: {}", keyword, e.getMessage());
            }
        }
        return naverArticles;
    }

    private List<Article> filterExistingArticles(List<Article> fetchedArticles) {
        // 1. 메모리 상의 중복 제거 (여러 키워드로 인해 중복 수집된 네이버 뉴스 등)
        Map<String, Article> uniqueArticlesMap = fetchedArticles.stream()
                .filter(article -> article.getSourceUrl() != null && !article.getSourceUrl().isBlank())
                .collect(Collectors.toMap(
                        Article::getSourceUrl,
                        article -> article,
                        (existing, replacement) -> existing // 중복 시 기존 것 유지
                ));

        List<Article> uniqueArticles = new ArrayList<>(uniqueArticlesMap.values());

        // 2. DB와 대조하여 이미 존재하는 URL 필터링 (IN 쿼리 사용)
        List<String> urlsToCheck = uniqueArticles.stream()
                .map(Article::getSourceUrl)
                .toList();

        if (urlsToCheck.isEmpty()) {
            return Collections.emptyList();
        }

        // DB에 존재하는 URL 목록 조회 (최적화를 위해 URL만 가져옵니다)
        List<String> existingUrls = articleRepository.findSourceUrlsBySourceUrlIn(urlsToCheck);

        // DB에 없는 새로운 기사만 필터링하여 반환
        return uniqueArticles.stream()
                .filter(article -> !existingUrls.contains(article.getSourceUrl()))
                .toList();
    }

    private String cleanHtmlTags(String text) {
        if (text == null) return "";
        // 정규식으로 HTML 태그(<b> 등) 제거
        String noTagText = text.replaceAll("<[^>]*>", "");
        // Spring HtmlUtils를 사용하여 모든 HTML 엔티티(&quot;, &amp;, &lt; 등)를 안전하게 디코딩
        return HtmlUtils.htmlUnescape(noTagText);
    }

    private LocalDateTime parseNaverDate(String pubDateStr) {
        try {
            // 타임존 정보를 포함하여 파싱한 뒤, 로컬 타임으로 변환
            return ZonedDateTime.parse(pubDateStr, NAVER_DATE_FORMATTER).toLocalDateTime();
        } catch (Exception e) {
            log.warn("[날짜 파싱 실패] 원본 날짜: {}, 에러: {}", pubDateStr, e.getMessage());
            return LocalDateTime.now();
        }
    }
}
