package com.lunazkoe.newsaggregator.domain.interest.service;

import com.lunazkoe.newsaggregator.domain.interest.dto.request.InterestRegisterRequest;
import com.lunazkoe.newsaggregator.domain.interest.dto.request.InterestUpdateRequest;
import com.lunazkoe.newsaggregator.domain.interest.dto.request.SubscriptionDto;
import com.lunazkoe.newsaggregator.domain.interest.dto.response.InterestDto;
import com.lunazkoe.newsaggregator.domain.interest.dto.request.InterestSearchCondition;
import com.lunazkoe.newsaggregator.domain.interest.entity.Interest;
import com.lunazkoe.newsaggregator.domain.interest.entity.Subscription;
import com.lunazkoe.newsaggregator.domain.interest.exception.InterestErrorCode;
import com.lunazkoe.newsaggregator.domain.interest.exception.InterestException;
import com.lunazkoe.newsaggregator.domain.interest.repository.InterestRepository;
import com.lunazkoe.newsaggregator.domain.interest.repository.SubscriptionRepository;
import com.lunazkoe.newsaggregator.domain.user.entity.User;
import com.lunazkoe.newsaggregator.domain.user.exception.UserErrorCode;
import com.lunazkoe.newsaggregator.domain.user.exception.UserException;
import com.lunazkoe.newsaggregator.domain.user.repository.UserRepository;
import com.lunazkoe.newsaggregator.global.common.dto.CursorPageResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class InterestService {

    private final InterestRepository interestRepository;
    private final UserRepository userRepository;
    private final SubscriptionRepository subscriptionRepository;

    @Transactional
    public InterestDto register(InterestRegisterRequest request) {
        log.info("Registering new interest: {}", request.name());

        // 중복 이름 검증 (유사도 기반)
        if (interestRepository.existsBySimilarName(request.name())) {
            log.warn("Interest name already exists: {}", request.name());
            throw new InterestException(InterestErrorCode.EXIST_SIMILARITY_NAME, Map.of("name", request.name()));
        }

        Interest newInterest = Interest.builder()
                .name(request.name())
                .keywords(request.keywords())
                .build();

        Interest savedInterest = interestRepository.save(newInterest);

        // 등록 직후이므로 구독 여부는 false로 설정
        return InterestDto.from(savedInterest, null);
        // - 왜 인지 모르겠지만 null로 반환이 되네
    }

    @Transactional
    public InterestDto update(UUID interestId, InterestUpdateRequest request) {
        log.info("Updating interest keywords for ID: {}", interestId);

        Interest foundInterest = interestRepository.findById(interestId)
                .orElseThrow(() -> {
                    log.warn("Interest not found with ID: {}", interestId);
                    return new InterestException(InterestErrorCode.INTEREST_NOT_FOUND, Map.of("id", interestId));
                });

        foundInterest.updateKeywords(request.keywords());
//        foundInterest.getKeywords().clear();
//        foundInterest.getKeywords().addAll(request.keywords());

        // TODO: 구독 여부를 확인하는 로직은 아직 안함
        // - 현재는 임시로 false 반환
        return InterestDto.from(foundInterest, false);
    }

    @Transactional
    public void hardDelete(UUID interestId) {
        log.info("Hard deleting interest ID: {}", interestId);

        Interest foundInterest = interestRepository.findById(interestId)
                .orElseThrow(() -> {
                    log.warn("Interest not found with ID: {}", interestId);
                    return new InterestException(InterestErrorCode.INTEREST_NOT_FOUND, Map.of("id", interestId));
                });

        // TODO: 연쇄 삭제 구현해야할 듯

        interestRepository.delete(foundInterest);
    }

    @Transactional(readOnly = true)
    public CursorPageResponse<InterestDto> searchInterests(
            InterestSearchCondition condition,
            UUID userId
    ) {
        log.info("Searching interests with condition: {}", condition);

        CursorPageResponse<Interest> pageResponse = interestRepository.searchInterests(condition);

        // TODO: 구독 도메인 구현 후, userId를 기반으로 해당 관심사를 구독했는지 확인 - 이때 userId를 사용
        List<InterestDto> dtoList = pageResponse.content().stream()
                .map(interest -> InterestDto.from(interest, false))
                .toList();// 임시로 false 반환 중

        return new CursorPageResponse<>(
                dtoList,
                pageResponse.nextCursor(),
                pageResponse.nextAfter(),
                pageResponse.size(),
                pageResponse.totalElements(),
                pageResponse.hasNext()
        );
    }

    // TODO
    // - 관심사 목록 조회
    // - 관심사 구독
    // - 관심사 구독 취소
    // - **구독에 대한 처리**

    @Transactional
    public SubscriptionDto subscribe(UUID interestId, UUID userId) {
        log.info("관심사 구독 요청 처리 시작 - interestId: {}, userId: {}", interestId, userId);

        Interest foundInterest = interestRepository.findById(interestId)
                .orElseThrow(() -> new InterestException(InterestErrorCode.INTEREST_NOT_FOUND, Map.of("id", interestId)));

        User foundUser = userRepository.findById(userId)
                .orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND, Map.of("id", userId)));

        if (subscriptionRepository.existsByUserIdAndInterestId(userId, interestId)) {
            throw new InterestException(InterestErrorCode.ALREADY_SUBSCRIBED);
        }

        Subscription newSubscription = Subscription.builder()
                .user(foundUser)
                .interest(foundInterest)
                .build();

        Subscription savedSubscription = subscriptionRepository.save(newSubscription);
        foundInterest.increaseSubscriberCount(); // 더티 체크로 업데이트됨

        log.info("관심사 구독 완료 - subscriptionId: {}", savedSubscription.getId());
        return SubscriptionDto.from(savedSubscription);
    }

    @Transactional
    public void cancelSubscription(UUID interestId, UUID userId) {
        log.info("관심사 구독 취소 요청 처리 시작 - interestId: {}, userId: {}", interestId, userId);

        Subscription subscription = subscriptionRepository.findByUserIdAndInterestId(userId, interestId)
                .orElseThrow(() -> new InterestException(InterestErrorCode.SUBSCRIPTION_NOT_FOUND));

        Interest interest = subscription.getInterest();
        interest.decreaseSubscriberCount(); // 더티 체크

        subscriptionRepository.delete(subscription);
        log.info("관심사 구독 취소 완료 - interestId: {}", interestId);
    }
}
