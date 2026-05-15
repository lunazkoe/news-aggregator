package com.lunazkoe.newsaggregator.domain.interest.service;

import com.lunazkoe.newsaggregator.domain.interest.dto.request.InterestRegisterRequest;
import com.lunazkoe.newsaggregator.domain.interest.dto.request.InterestUpdateRequest;
import com.lunazkoe.newsaggregator.domain.interest.dto.response.SubscriptionDto;
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
import com.lunazkoe.newsaggregator.global.common.event.SubscriptionEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
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
    private final ApplicationEventPublisher eventPublisher;

    /**
     * 관심사 목록 조회
     */
    @Transactional(readOnly = true)
    public CursorPageResponse<InterestDto> searchInterests(
            InterestSearchCondition condition,
            UUID requestUserId
    ) {
        log.info("Searching interests with condition: {}", condition);

        CursorPageResponse<Interest> pageResponse = interestRepository.searchInterests(condition);

        // TODO: N+1 문제 발생 예상 지점
        // - 50개를 가져오는 쿼리 1개 실행 + 각 interest마다 구독여부 확인 쿼리 N개 실행될 것으로 예상됨
        List<InterestDto> dtoList = pageResponse.content().stream()
                .map(interest -> {
                            boolean subscribedByMe = subscriptionRepository.existsByUserIdAndInterestId(requestUserId, interest.getId());
                            return InterestDto.from(interest, subscribedByMe);
                    }
                )
                .toList();

        return new CursorPageResponse<>(
                dtoList,
                pageResponse.nextCursor(),
                pageResponse.nextAfter(),
                pageResponse.size(),
                pageResponse.totalElements(),
                pageResponse.hasNext()
        );
    }

    /**
     * 관심사 등록
     */
    @Transactional
    public InterestDto register(InterestRegisterRequest request) {
        log.info("Registering new interest: {}", request.name());

        // TODO: 중복 이름 검증 (유사도 기반)
        if (interestRepository.existsBySimilarName(request.name())) {
            log.warn("Interest name already exists: {}", request.name());
            throw new InterestException(InterestErrorCode.EXIST_SIMILARITY_NAME, Map.of("name", request.name()));
        }

        Interest newInterest = Interest.builder()
                .name(request.name())
                .keywords(request.keywords())
                .build();

        Interest savedInterest = interestRepository.save(newInterest);

        // TODO: 등록 직후이므로 구독 여부는 false로 설정
        // - 왜 인지 모르겠지만 API Spec에서 테스트하니깐 null로 반환이 되네
        return InterestDto.from(savedInterest, false);
    }

    /**
     * 관심사 구독
     */
    @Transactional
    public SubscriptionDto subscribe(UUID interestId, UUID userId) {
        log.info("관심사 구독 요청 처리 시작 - interestId: {}, userId: {}", interestId, userId);

        Interest foundInterest = interestRepository.findById(interestId)
                .orElseThrow(() -> new InterestException(InterestErrorCode.INTEREST_NOT_FOUND, Map.of("id", interestId)));

        User foundUser = userRepository.findById(userId)
                .orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND, Map.of("id", userId)));

        // TODO: 이미 관심사를 구독했다면 예외를 던질 것인지? 아니면 이전 값을 던질 것인지 고민
        if (subscriptionRepository.existsByUserIdAndInterestId(userId, interestId)) {
            throw new InterestException(InterestErrorCode.ALREADY_SUBSCRIBED);
        }

        Subscription newSubscription = Subscription.builder()
                .user(foundUser)
                .interest(foundInterest)
                .build();

        Subscription savedSubscription = subscriptionRepository.save(newSubscription);

        // 구독자 수 증가!!
        foundInterest.increaseSubscriberCount(); // 더티 체크로 업데이트됨

        log.info("관심사 구독 완료 - subscriptionId: {}", savedSubscription.getId());

        // 여기서 추가 쿼리가 발생하지 않는 이유
        // - Subscription은 User / Interest를 지연로딩으로 들고 있음
        // - 바로 위에서 savedSubscritpon을 만들 때 interest를 끼워 넣어서 그 객체를 넣는 것이기 때문에
        // - 추가 쿼리가 발생하지 않음

        // 관심사 구독 시 이벤트 발행
        eventPublisher.publishEvent(new SubscriptionEvent(
                foundUser.getId(),
                newSubscription.getId(),
                foundInterest.getId(),
                foundInterest.getName(),
                foundInterest.getKeywords(),
                foundInterest.getSubscriberCount(),
                newSubscription.getCreatedAt()
        ));

        return SubscriptionDto.from(savedSubscription);
    }

    /**
     * 관심사 구독 취소
     */
    @Transactional
    public void cancelSubscription(UUID interestId, UUID userId) {
        log.info("관심사 구독 취소 요청 처리 시작 - interestId: {}, userId: {}", interestId, userId);

        // TODO: 구독을 하지 않았었는데 취소 요청을 보낼 경우 예외? 그냥 종료?
        // TODO: FETCH JOIN으로 쿼리 한 개로 최적화할 수 있을듯
        Subscription subscription = subscriptionRepository.findByUserIdAndInterestId(userId, interestId)
                .orElseThrow(() -> new InterestException(InterestErrorCode.SUBSCRIPTION_NOT_FOUND));

        Interest interest = subscription.getInterest();

        // 구독자 수 감소
        interest.decreaseSubscriberCount(); // 더티 체크

        subscriptionRepository.delete(subscription);
        log.info("관심사 구독 취소 완료 - interestId: {}", interestId);
    }

    /**
     * 물리 삭제
     */
    @Transactional
    public void hardDelete(UUID interestId) {
        log.info("Hard deleting interest ID: {}", interestId);

        Interest foundInterest = interestRepository.findById(interestId)
                .orElseThrow(() -> {
                    log.warn("Interest not found with ID: {}", interestId);
                    return new InterestException(InterestErrorCode.INTEREST_NOT_FOUND, Map.of("id", interestId));
                });

        // TODO: 물리 삭제 시 발생할 수 있는 문제들(연쇄 삭제 등 아직 미구현)

        interestRepository.delete(foundInterest);
    }

    /**
     * 관심사 정보 수정
     */
    // TODO: 여기 뭔가 문제 있는데? Header가 안들어오면 어떻게 구독했는지 정보를 어디서 가져와야하는 걸까? 사용자 내역에서?
    @Transactional
    public InterestDto update(UUID interestId, InterestUpdateRequest request, UUID requestUserId) {
        log.info("Updating interest keywords for ID: {}", interestId);

        Interest foundInterest = interestRepository.findById(interestId)
                .orElseThrow(() -> {
                    log.warn("Interest not found with ID: {}", interestId);
                    return new InterestException(InterestErrorCode.INTEREST_NOT_FOUND, Map.of("id", interestId));
                });

        foundInterest.updateKeywords(request.keywords());

        boolean subscribedByMe = subscriptionRepository.existsByUserIdAndInterestId(requestUserId, interestId);

        return InterestDto.from(foundInterest, subscribedByMe);
    }
}
