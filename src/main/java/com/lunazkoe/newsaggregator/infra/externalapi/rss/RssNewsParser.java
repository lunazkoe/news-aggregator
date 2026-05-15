package com.lunazkoe.newsaggregator.infra.externalapi.rss;

import com.lunazkoe.newsaggregator.infra.externalapi.rss.dto.RssNewsItem;
import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.net.URL;
import java.util.Collections;
import java.util.List;

@Slf4j
@Component
public class RssNewsParser {

    public List<RssNewsItem> parse(String rssUrl) {
        try {
            URL feedUrl = new URL(rssUrl);
            SyndFeedInput input = new SyndFeedInput();
            SyndFeed feed = input.build(new XmlReader(feedUrl));

            return feed.getEntries().stream()
                    .map(this::mapToDto)
                    .toList();
        } catch (Exception e) {
            log.error("[RSS 파싱 실패] URL: {}, 원인: {}", rssUrl, e.getMessage());
            return Collections.emptyList(); // 장애 전파를 막기 위해 빈 리스트 반환
        }
    }

    private RssNewsItem mapToDto(SyndEntry entry) {
        return new RssNewsItem(
                entry.getTitle(),
                entry.getLink(),
                entry.getDescription() != null ? entry.getDescription().getValue() : "",
                entry.getPublishedDate()
        );
    }
}
