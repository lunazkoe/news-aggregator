package com.lunazkoe.newsaggregator.domain.interest.service;

import com.lunazkoe.newsaggregator.domain.interest.dto.request.InterestRegisterRequest;
import com.lunazkoe.newsaggregator.domain.interest.dto.request.InterestUpdateRequest;
import com.lunazkoe.newsaggregator.domain.interest.dto.response.InterestDto;
import com.lunazkoe.newsaggregator.domain.interest.entity.Interest;
import com.lunazkoe.newsaggregator.domain.interest.exception.InterestErrorCode;
import com.lunazkoe.newsaggregator.domain.interest.exception.InterestException;
import com.lunazkoe.newsaggregator.domain.interest.repository.InterestRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class InterestService {

    private final InterestRepository interestRepository;

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

        interestRepository.delete(foundInterest);
    }

    // public CursorPageResponseInterestDto search(...) {
    //     TODO: Day 4 / Day 5 커서 기반 페이징 구현 시 작성
    //     return null;
    // }

    // TODO
    // - 관심사 목록 조회
    // - 관심사 구독
    // - 관심사 구독 취소
    // - **구독에 대한 처리**
}
