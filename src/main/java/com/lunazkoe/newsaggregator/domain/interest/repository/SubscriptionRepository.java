package com.lunazkoe.newsaggregator.domain.interest.repository;

import com.lunazkoe.newsaggregator.domain.interest.entity.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SubscriptionRepository extends JpaRepository<Subscription, UUID> {

    // 특정 유저가 관심사를 구독 중인지 확인
    boolean existsByUserIdAndInterestId(UUID userId, UUID interestId);

    // 특정 유저와 관심사 ID로 구독 정보 조회 (구독 취소 시 사용)
    Optional<Subscription> findByUserIdAndInterestId(UUID userId, UUID interestId);

    List<Subscription> findTop10ByUserIdOrderByCreatedAtDesc(UUID userId);
}
