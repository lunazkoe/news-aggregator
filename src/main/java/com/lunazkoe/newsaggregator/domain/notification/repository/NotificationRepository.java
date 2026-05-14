package com.lunazkoe.newsaggregator.domain.notification.repository;

import com.lunazkoe.newsaggregator.domain.notification.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface NotificationRepository extends JpaRepository<Notification, UUID>, NotificationRepositoryCustom {

//    List<Notification> findAllByUserIdAndConfirmedFalse(UUID userId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    // - 영속성 컨텍스트 문제:
    //      - 벌크 업데이트 시 DB에 직접 쿼리를 실행
    //      - 트랜잭션 내에서 알림 데이터를 1차 캐시에 올려둔 상태일 경우
    //      - DB 데이터는 변경되지만, 영속성 컨텍스트에 있는 객체는 변경이 안됨
    //      - 이 때 조회 시 영속성 컨텍스트에 있는 데이터를 가져오면 데이터 불일치 문제가 발생할 수 있음
    // => clearAutomatically: 벌크 연산 실행 후 영속성 컨텍스트를 초기화
    // - Flush 누락 문제:
    //      - 그전에 영속성 컨텍스트에서 변경되어 아직 DB에 반영도지 않은 다른 작업들과 순서가 꼬일 수 있음
    // => flushAutomatically: 벌크 연산을 실행하기 전에 영속성 컨텍스트의 변경 사항을 DB에 먼저 동기화
    @Query("UPDATE Notification n " +
            "SET n.confirmed = true, n.updatedAt = :now " + // DB 시간이 아닌 파라미터로 받은 App 시간 사용
            "WHERE n.user.id = :userId AND n.confirmed = false")
    // - JPA Auditing 기능 우회 문제
    //      - 벌크 연산 시 updatedAt 시간은 갱신되지 않음 (직접 DB에 쿼리를 실행해서)
    //      - 갱신 시간을 직접 변경해주어야 함
    // - DB 시간과 어플리케이션 시간 불일치 문제
    //      - 직접 Now를 호출해서 해당 시점의 시간을 넣어줌
    int markAllAsRead(@Param("userId") UUID userId, @Param("now") LocalDateTime now);
}
