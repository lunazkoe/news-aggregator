package com.lunazkoe.newsaggregator.domain.article.entity;

import com.lunazkoe.newsaggregator.global.common.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "articles")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@SQLRestriction("is_deleted = false")
public class Article extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Source source;

    @Column(nullable = false)
    private String sourceUrl;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String summary;

    @Column(nullable = false)
    private LocalDateTime publishDate;

    @Column(nullable = false)
    private Integer viewCount = 0;

    @Column(nullable = false)
    private Integer commentCount = 0;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Column(nullable = false)
    private Boolean isDeleted = false;

    @Builder
    public Article(Source source, String sourceUrl, String title, String summary, LocalDateTime publishDate) {
        this.source = source;
        this.sourceUrl = sourceUrl;
        this.title = title;
        this.summary = summary;
        this.publishDate = publishDate;
    }

    public void softDelete() {
        this.deletedAt = LocalDateTime.now();
        this.isDeleted = true;
    }

    public void increaseViewCount() {
        if (this.viewCount < Integer.MAX_VALUE) {
            this.viewCount++;
        }
    }

    public void increaseCommentCount() {
        if (this.commentCount < Integer.MAX_VALUE) {
            this.commentCount++;
        }
    }

    public void decreaseCommentCount(){
        if (this.commentCount > 0) {
            this.commentCount--;
        }
    }
}
