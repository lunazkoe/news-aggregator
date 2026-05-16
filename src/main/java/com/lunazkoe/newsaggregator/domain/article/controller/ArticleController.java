package com.lunazkoe.newsaggregator.domain.article.controller;

import com.lunazkoe.newsaggregator.domain.article.dto.request.ArticleSearchCondition;
import com.lunazkoe.newsaggregator.domain.article.dto.response.ArticleDto;
import com.lunazkoe.newsaggregator.domain.article.dto.response.ArticleRestoreResultDto;
import com.lunazkoe.newsaggregator.domain.article.dto.response.ArticleViewDto;
import com.lunazkoe.newsaggregator.domain.article.entity.Source;
import com.lunazkoe.newsaggregator.domain.article.service.ArticleService;
import com.lunazkoe.newsaggregator.global.common.dto.CursorPageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static com.lunazkoe.newsaggregator.global.filter.MDCLoggingFilter.*;

@Slf4j
@RestController
@RequestMapping("/api/articles")
@RequiredArgsConstructor
public class ArticleController {

    private final ArticleService articleService;

    @Operation(summary = "기사 뷰 등록", description = "기사 뷰를 등록합니다.")
    @PostMapping("/{articleId}/article-views")
    @ResponseStatus(HttpStatus.OK)
    public ArticleViewDto recordView(@PathVariable UUID articleId, @RequestHeader(HEADER_USER_ID) UUID userId) {
        ArticleViewDto response = articleService.recordArticleView(articleId, userId);
        return response;
    }

    @Operation(summary = "뉴스 기사 목록 조회", description = "조건에 맞는 뉴스 기사 목록을 조회합니다.")
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public CursorPageResponse<ArticleDto> searchArticles(
            @ModelAttribute ArticleSearchCondition condition,
            @RequestHeader(HEADER_USER_ID) UUID requestUserId
    ) {
        log.info("articleCondition={}", condition);
        CursorPageResponse<ArticleDto> response = articleService.searchArticles(condition, requestUserId);
        return response;
    }

    @Operation(summary = "뉴스 기사 단건 조회", description = "뉴스 기사 ID로 뉴스 기사 단건을 조회합니다.")
    @GetMapping("/{articleId}")
    @ResponseStatus(HttpStatus.OK)
    public ArticleDto getArticle(@PathVariable UUID articleId, @RequestHeader(HEADER_USER_ID) UUID requestUserId) {
        ArticleDto response = articleService.getArticle(articleId, requestUserId);
        return response;
    }

    @Operation(summary = "뉴스 기사 논리 삭제", description = "뉴스 기사를 논리적으로 삭제합니다.")
    @DeleteMapping("/{articleId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void softDeleteArticle(@PathVariable UUID articleId) {
        articleService.softDeleteArticle(articleId);
    }

    @Operation(summary = "출처 목록 조회", description = "출처(Enum) 목록을 조회합니다.")
    @GetMapping("/sources")
    @ResponseStatus(HttpStatus.OK)
    public List<String> getSources() {
        List<String> response = Arrays.stream(Source.values())
                .map(Enum::name)
                .toList();
        return response;
    }

    @Operation(summary = "뉴스 복구", description = "유실된 뉴스 기사를 복구")
    @GetMapping("/restore")
    @ResponseStatus(HttpStatus.OK)
    public List<ArticleRestoreResultDto> restoreArticles(
            @RequestParam("from") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam("to") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to
    ) {
        log.info("Article restore API called. from: {}, to: {}", from, to);
        List<ArticleRestoreResultDto> response = articleService.restoreArticles(from, to);
        return response;
    }

    @Operation(summary = "뉴스 기사 물리 삭제", description = "뉴스 기사를 물리적으로 삭제합니다.")
    @DeleteMapping("/{articleId}/hard")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void hardDeleteArticle(@PathVariable UUID articleId) {
        articleService.hardDeleteArticle(articleId);
    }
}
