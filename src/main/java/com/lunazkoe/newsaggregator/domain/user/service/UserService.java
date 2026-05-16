package com.lunazkoe.newsaggregator.domain.user.service;

import com.lunazkoe.newsaggregator.domain.user.dto.request.UserLoginRequest;
import com.lunazkoe.newsaggregator.domain.user.dto.request.UserRegisterRequest;
import com.lunazkoe.newsaggregator.domain.user.dto.request.UserUpdateRequest;
import com.lunazkoe.newsaggregator.domain.user.dto.response.UserDto;
import com.lunazkoe.newsaggregator.domain.user.entity.User;
import com.lunazkoe.newsaggregator.domain.user.exception.UserErrorCode;
import com.lunazkoe.newsaggregator.domain.user.exception.UserException;
import com.lunazkoe.newsaggregator.domain.user.repository.UserRepository;
import com.lunazkoe.newsaggregator.global.common.event.UserRegisteredEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * 등록(회원가입)
     */
    @Transactional
    public UserDto register(UserRegisterRequest request) {
        // 이메일 중복 체크
        // - 논리 삭제된 User는 여기를 통과하게 됨 => 현재 정책: 논리 삭제를 복구 X / 완전히 새로운 계정 생성
        if (userRepository.existsByEmail(request.email())) {
            throw new UserException(UserErrorCode.EMAIL_ALREADY_EXISTS, Map.of("email", request.email()));
        }

        // TODO: 실제 사용 환경에서는 PasswordEncoder로 암호화를 해주어야함(Bcrypt)
        User newUser = User.builder()
                .email(request.email())
                .nickname(request.nickname())
                .password(request.password())
                .build();

        User savedUser = userRepository.save(newUser);
        log.info("User registered successfully. UserId: {}", savedUser.getId());

        // 회원가입이 끝나는 시점에 mongodb에 유저 활동 내역(빈 상태로) 발행
        eventPublisher.publishEvent(new UserRegisteredEvent(
                savedUser.getId(),
                savedUser.getEmail(),
                savedUser.getNickname()
        ));

        return UserDto.from(savedUser);
    }

    /**
     * 로그인
     */
    @Transactional(readOnly = true)
    public UserDto login(UserLoginRequest request) {
        // 아이디(이메일) 일치(존재) 여부 확인
        User foundUser = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new UserException(UserErrorCode.EMAIL_OR_PASSWORD_INVALID));

        // 비밀번호 일치 여부 확인
        // TODO: PasswordEncoder로 암호화를 했을 경우 복호화를 해야함(진짜 복호화는 아님)
        if (!foundUser.getPassword().equals(request.password())) {
            throw new UserException(UserErrorCode.EMAIL_OR_PASSWORD_INVALID);
        }

        log.info("User login successfully, UserId: {}", foundUser.getId());

        return UserDto.from(foundUser);
    }

    /**
     * 논리 삭제
     */
    @Transactional
    public void softDelete(UUID userId, UUID requestUserId) {
        // 삭제 권환 확인
        // - 현재 API Spec에 requestUserId를 받는 건 없음
        // - RequestHeader에 로그인한 사용자의 ID를 받을 수 있다는 건 명시
        // - API 스펙에서도 삭제 권한 확인 예외를 던지는 것을 명시
        validateAuthorized(userId, requestUserId);

        User foundUser = userRepository.findById(userId)
                .orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND, Map.of("id", userId)));

        // 논리 삭제 (더티 체킹)
        foundUser.softDelete();

        log.info("User soft delete successfully. UserId: {}", userId);
    }

    /**
     * 사용자 정보(닉네임만 수정 가능)
     */
    @Transactional
    public UserDto updateNickName(
            UUID userId,
            UserUpdateRequest request
//            UUID requestUserId
    ) {
//        validateAuthorized(userId, requestUserId);

        User foundUser = userRepository.findById(userId)
                .orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND, Map.of("id", userId)));

        foundUser.updateNickName(request.nickname());

        log.info("User nickname updated: UserId: {}", foundUser.getId());

        return UserDto.from(foundUser);
    }

    /**
     * 물리 삭제
     * TODO: 연관된 모든 객체 삭제 로직 미완성 (Cascade 설정 등을 아직 하지 않음)
     * TODO: 나중에 30일 된 유저 일괄 처리같은 경우 "벌크 작업"
     * - 지금은 그냥 이대로 유지. 바로 완전 삭제를 한다는 의미로만 남겨두기
     * - 문제: 논리 삭제는 조회가 안되어, 물리 삭제를 할 수 없음
     *      - 해결: 일단 스펙 명세를 논리 삭제 또는 물리 삭제 중 사용자가 딱 하나만 선택할 수 있음
     *      - 즉, 논리 삭제 후 물리 삭제를 명시적으로 할 수 없음
     */
    @Transactional
    public void hardDelete(UUID userId, UUID requestUserId) {

        validateAuthorized(userId, requestUserId);

        User foundUser = userRepository.findById(userId)
                .orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND, Map.of("id", userId)));

        userRepository.delete(foundUser);

        log.info("User hard delete successfully. UserId: {}", userId);
    }

    private void validateAuthorized(UUID userId, UUID requestId) {
        // TODO: 근데 결국 이렇게 검증하는 것은 보안 이슈가 필연적으로 발생할 수 밖에 없음
        if (!userId.equals(requestId)) {
            throw new UserException(UserErrorCode.UNAUTHORIZED_ACTION);
        }
    }
}
