package com.lunazkoe.newsaggregator.domain.user.service;

import com.lunazkoe.newsaggregator.domain.user.dto.request.UserLoginRequest;
import com.lunazkoe.newsaggregator.domain.user.dto.request.UserRegisterRequest;
import com.lunazkoe.newsaggregator.domain.user.dto.request.UserUpdateRequest;
import com.lunazkoe.newsaggregator.domain.user.dto.response.UserDto;
import com.lunazkoe.newsaggregator.domain.user.entity.User;
import com.lunazkoe.newsaggregator.domain.user.exception.UserErrorCode;
import com.lunazkoe.newsaggregator.domain.user.exception.UserException;
import com.lunazkoe.newsaggregator.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    /**
     * 회원가입
     */
    @Transactional
    public UserDto register(UserRegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new UserException(UserErrorCode.EMAIL_ALREADY_EXISTS);
        }

        // TODO: 실제 사용 환경에서는 PasswordEncoder로 암호화를 해주어야함(BCrypt)
        // 일단은 Pass
        User newUser = User.builder()
                .email(request.email())
                .nickname(request.nickname())
                .password(request.password())
                .build();

        User savedUser = userRepository.save(newUser);
        log.info("User registered successfully. UserId: {}", savedUser.getId());

        return UserDto.from(savedUser);
    }

    /**
     * 로그인
     */
    @Transactional(readOnly = true)
    public UserDto login(UserLoginRequest request) {
        User foundUser = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND));

        if (!foundUser.getPassword().equals(request.password())) {
            throw new UserException(UserErrorCode.INVALID_PASSWORD);
            // - 사실 비밀번호가 잘못되었다고 알려주는 것은 좋은게 아니라고 하긴하는데
            // - 근데 꼭 그렇지만은 않은 것 같기도?
        }

        log.info("User login successfully, UserId: {}", foundUser.getId());

        return UserDto.from(foundUser);
    }

    /**
     * 사용자 정보(닉네임 수정)
     */
    @Transactional
    public UserDto updateNickName(UUID userId, UserUpdateRequest request) {
        User foundUser = userRepository.findById(userId)
                .orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND));

        foundUser.updateNickName(request.nickname());
        log.info("User nickname updated: UserId: {}", foundUser.getId());

        return UserDto.from(foundUser);
    }

    /**
     * 논리 삭제
     */
    @Transactional
    public void delete(UUID userId) {
        User foundUser = userRepository.findById(userId)
                .orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND));

//        userRepository.delete(foundUser); // 여기서 Update 쿼리로 변화되어서 실행됨
        // - 여기서 deleteById를 사용하지 않은 이유
        // - deleteById로 하면 id로 select 쿼리를 날리고 찾아서 delete를 함
        // - 근데 위에서 findById로 이미 select을 날렸는데, 불필요하게 더 날릴 필요가 없음

        foundUser.softDelete();
        // - 더티 체킹으로 변경

        log.info("User soft delete successfully. UserId: {}", userId);
    }

    /**
     * 물리 삭제
     * TODO: 나중에 30일 된 유저 일괄 처리같은 경우 "벌크 작업" 고민
     * - 지금은 그냥 이대로 유지. 바로 완전 삭제를 한다는 의미로만 남겨두기
     */
    @Transactional
    public void hardDelete(UUID userId) {
        // 여기서의 고민 포인트는
        // - 이 물리적 삭제를 할 때, SQLRestriction을 걸어놔서 논리 삭제가 되어있으면 삭제가 안될텐데
        // - 다만 요구사항에 논리 삭제 이후 시간이 지나면 완전 삭제일 경우를 생각해볼 수 있는데
        // - 그러면 즉시 삭제가 없다는거니깐, hardDeleteById여기에 조건은 추가해야되지 않나라는 생각
        //      - where id = :id and is_deleted = true 인경우만 삭제하게 해야되는거 아닌가?
//        User foundUser = userRepository.findById(userId)
//                .orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND));

        userRepository.hardDeleteById(userId);
        log.info("User hard delete successfully. UserId: {}", userId);
    }
}
