package com.lunazkoe.newsaggregator.domain.interest.entity;

import com.lunazkoe.newsaggregator.global.common.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Getter
@Table(name = "interests")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SQLRestriction("is_deleted = false")
// - 이거 오타낼 수도 있으니깐 나중에 커스텀 어노테이션으로 만들어보자
public class Interest extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true, length = 50)
    private String name;

    @JdbcTypeCode(SqlTypes.ARRAY)
    // - Hibernate 6 기능: PostgreSQL의 배열 타입(varchar[])와 Java의 List를 쉽게 매핑
    @Column(columnDefinition = "varchar[]")
    private List<String> keywords = new ArrayList<>();

    @Column(name = "subscriber_count", nullable = false)
    private Integer subscriberCount = 0;

    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted = false;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Builder
    public Interest(String name, List<String> keywords) {
        this.name = name;
        this.keywords = keywords != null ? keywords : new ArrayList<>();
    }

    public void updateKeywords(List<String> keywords) {
        this.keywords = keywords; // AI가 이것보다는
//        this.keywords.clear();
//        this.keywords.addAll(keywords); // 가 낫다고 하네요
    }

    public void increaseSubscriberCount() {
        if (this.subscriberCount < Integer.MAX_VALUE) {
            this.subscriberCount++;
        }
    }

    public void decreaseSubscriberCount() {
        if (this.subscriberCount > 0) {
            this.subscriberCount--;
        }
    }
}
