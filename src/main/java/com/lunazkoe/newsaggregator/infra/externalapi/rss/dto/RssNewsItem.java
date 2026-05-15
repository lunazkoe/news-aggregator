package com.lunazkoe.newsaggregator.infra.externalapi.rss.dto;

import java.util.Date;

public record RssNewsItem(
        String title,
        String link,
        String description,
        Date pubDate
) {
}
