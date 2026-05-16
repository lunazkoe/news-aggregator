package com.lunazkoe.newsaggregator.batch.collector;

import com.lunazkoe.newsaggregator.domain.article.entity.Article;
import com.lunazkoe.newsaggregator.domain.article.entity.Source;
import com.lunazkoe.newsaggregator.domain.article.repository.ArticleRepository;
import com.lunazkoe.newsaggregator.domain.interest.entity.Interest;
import com.lunazkoe.newsaggregator.domain.interest.entity.Subscription;
import com.lunazkoe.newsaggregator.domain.interest.repository.InterestRepository;
import com.lunazkoe.newsaggregator.domain.interest.repository.SubscriptionRepository;
import com.lunazkoe.newsaggregator.domain.notification.entity.Notification;
import com.lunazkoe.newsaggregator.domain.notification.entity.ResourceType;
import com.lunazkoe.newsaggregator.global.common.event.NotificationCreateEvent;
import com.lunazkoe.newsaggregator.infra.externalapi.naver.client.NaverNewsClient;
import com.lunazkoe.newsaggregator.infra.externalapi.naver.dto.NaverNewsItem;
import com.lunazkoe.newsaggregator.infra.externalapi.naver.dto.NaverNewsResponse;
import com.lunazkoe.newsaggregator.infra.externalapi.rss.RssNewsParser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
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
    private final ApplicationEventPublisher eventPublisher;
    private final TransactionTemplate transactionTemplate;
    // - 이벤트를 발행해야하는데 트랜잭션이 종료되고 발행된 이벤트를 실행해야하기 때문에 트랜잭션이 있어야하긴 함
    // - 근데 전체에 트랜잭션을 걸면 문제가 발생할 수 있기 때문에 따로 열어주기
    // - 궁금증: 그냥 저장하는 메소드를 따로 만들고 @Transactional을 메서드에 붙여주면 되는 거 아닌가?

    // 한국경제 RSS URL (예시임)
    private static final String HANKYUNG_RSS_URL = "https://rss.hankyung.com/feed/it.xml";

    // 네이버 날짜 포맷 파싱용
    private static final DateTimeFormatter NAVER_DATE_FORMATTER = DateTimeFormatter.RFC_1123_DATE_TIME;
    private final SubscriptionRepository subscriptionRepository;

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
            List<Interest> interests = interestRepository.findAll().stream().toList();

            if (interests.isEmpty()) {
                log.info("[뉴스 수집 배치] 등록된 관심사가 없어 수집을 생략합니다.");
                return;
            }

            // 관심사 단위로 루프를 돌며 뉴스를 수집
            for (Interest interest : interests) {
                List<Article> fetchedArticles = new ArrayList<>();

                // 해당 관심사에 속한 키워드들로 API 호출 (트랜잭션 없이 진행됨 => 커넥션 안전)
                for (String keyword : interest.getKeywords()) {
                    fetchedArticles.addAll(collectFromNaver(keyword));
                }

                // 중복 필터링
                List<Article> articlesToSave = filterExistingArticles(fetchedArticles);

                // 저장할 기사가 있다면 저장 + 이벤트 발행 구간만 트랜잭션으로 묶어줌
                if (!articlesToSave.isEmpty()) {
                    transactionTemplate.executeWithoutResult(status -> {
                        // 기사 저장
                        articleRepository.saveAll(articlesToSave);
                        log.info("[뉴스 수집 배치] '{}' 관련 새로운 뉴스 {}건 DB 적재 완료.", interest.getName(), articlesToSave.size());

                        // 알림 발송: 해당 관심사를 구독 중인 유저 조회
                        List<Subscription> subscriptions = subscriptionRepository.findAllByInterestId(interest.getId());

                        if (!subscriptions.isEmpty()) {
                            String content = String.format("[%s]와 관련된 기사가 %d건 등록되었습니다.", interest.getName(), articlesToSave.size());

                            for (Subscription sub : subscriptions) {
                                eventPublisher.publishEvent(new NotificationCreateEvent(
                                        sub.getUser().getId(), // TODO: N+1 문제 발생하지 않나?
                                        content,
                                        ResourceType.INTEREST,
                                        interest.getId()
                                ));
                                // TODO: 여기서 이벤트를 발행을 하고 비동기로 실행되는 건 이해햇는데, 너무 많은 유저에게 일일히 하면 뭔가 성능 문제가 있진 않을까?
                            }
                            log.info("[Event Published] 관심사 '{}' 새 기사 알림 이벤트 발행 완료. 발송 대상: {}명", interest.getName(), subscriptions.size());
                        }
                    });
                }
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

    // TODO: 각 관심사에 키워드가 여러 개 등록 될 수 있음
    // - 예를 들어 100개의 관심사에 각 키워드가 100개씩 있다면?
    // - 100 * 100번 검색을 시도해야하는 것이 아닌가? 이게 맞나?
    private List<Article> collectFromNaver(String keyword) {
        List<Article> naverArticles = new ArrayList<>();
        try {
            NaverNewsResponse response = naverNewsClient.searchNews(
                    naverClientId, naverClientSecret, keyword, 10, 1, "date");

            if (response != null && response.items() != null) {
                for (NaverNewsItem item : response.items()) {
                    naverArticles.add(Article.builder()
                            .source(Source.NAVER)
                            .sourceUrl(item.originallink())
                            .title(cleanHtmlTags(item.title()))
                            .summary(cleanHtmlTags(item.description()))
                            .publishDate(parseNaverDate(item.pubDate()))
                            .build());
                }
            }
        } catch (Exception e) {
            log.warn("[Naver API 수집 실패] 키워드: {}, 에러: {}", keyword, e.getMessage());
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
