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
public class Interest extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    // TODO: 관심사 이름의 중복을 허용하지 않음 => 이걸 유사도 기반으로 측정을 하려고 하는데 꼭 필요한 것일까에 대한 고민 + soft delete 시 발생할 수 있는 문제점(근데 논리 삭제를 지원하진 않음)
    @Column(nullable = false, unique = true, length = 50)
    private String name;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(columnDefinition = "varchar[]")
    private List<String> keywords = new ArrayList<>();

    @Column(name = "subscriber_count", nullable = false)
    private Integer subscriberCount = 0;

    // TODO: 개발 진행이 거의 완료 된 후(배포 직전) 이거 두 개는 API 스펙상 사용되진 않으므로 정말 상관이 없다면 삭제
    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted = false;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Builder
    public Interest(String name, List<String> keywords) {
        this.name = name;
        this.keywords = keywords != null ? keywords : new ArrayList<>();
    }

    // TODO: 키워드 업데이트 로직을 어떻게 하는 것이 좋을지 알아보고 선택하기
    public void updateKeywords(List<String> keywords) {
        this.keywords = keywords; // AI가 이것보다는
//        this.keywords.clear();
//        this.keywords.addAll(keywords); // 가 낫다고 하네요
        // - @OneToMany나 @ElementCollection을 사용하여 JPA가 컬렉션의 상태 변화를 추적해야할 때 문제가 발생할 수 있음
        // - 근데 현재 @JdbcTypeCode(SqlTypes.ARRAY)를 사용하여 네이티브 배열 타입을 통째로 매핑하고 있어서 단일 값처럼 취급하기 때문에 참조를 통째로 갈아끼워도 문제가 발생하지 않음
    }

    // TODO: increase / decrease에서 동시성 문제가 발생할 수 있음
    // - @Version을 사용한 낙관적 락 사용
    // - 데이터베이스에 직접 락을 거는 비관적 락 사용
    // - 엔티티를 조회하여 수정하는 대신, JPQL이나 직접 쿼리를 날려 원자적으로 값을 증가
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
