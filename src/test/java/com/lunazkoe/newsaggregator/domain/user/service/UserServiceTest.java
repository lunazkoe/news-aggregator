package com.lunazkoe.newsaggregator.domain.user.service;

import com.lunazkoe.newsaggregator.domain.user.dto.request.UserLoginRequest;
import com.lunazkoe.newsaggregator.domain.user.dto.request.UserRegisterRequest;
import com.lunazkoe.newsaggregator.domain.user.dto.request.UserUpdateRequest;
import com.lunazkoe.newsaggregator.domain.user.dto.response.UserDto;
import com.lunazkoe.newsaggregator.domain.user.entity.User;
import com.lunazkoe.newsaggregator.domain.user.exception.UserErrorCode;
import com.lunazkoe.newsaggregator.domain.user.exception.UserException;
import com.lunazkoe.newsaggregator.domain.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @InjectMocks
    private UserService userService;

    @Mock
    private UserRepository userRepository;

    // 테스트용 픽스처(Fixture) 생성
    private User createTestUser() {
        User user = User.builder()
                .email("test@monew.com")
                .password("password123")
                .nickname("tester")
                .build();
        // ID 강제 주입을 위해 Reflection을 사용하거나, 엔티티 구조상 허용된다면 단순 객체 비교
        // 테스트 편의성을 위해 여기서는 저장 시 객체를 그대로 반환하는 로직으로 짭니다.
        return user;
    }

    @Nested
    @DisplayName("회원가입 테스트")
    class RegisterTest {

        @Test
        @DisplayName("성공: 유효한 요청 시 유저가 등록된다.")
        void success() {
            // given
            UserRegisterRequest request = new UserRegisterRequest("test@monew.com", "tester", "password123");
            given(userRepository.existsByEmail(request.email())).willReturn(false);

            User mockUser = createTestUser();
            given(userRepository.save(any(User.class))).willReturn(mockUser);

            // when
            UserDto result = userService.register(request);

            // then
            assertThat(result.email()).isEqualTo(request.email());
            assertThat(result.nickname()).isEqualTo(request.nickname());
            verify(userRepository).save(any(User.class));
        }

        @Test
        @DisplayName("실패: 이미 존재하는 이메일이면 예외가 발생한다.")
        void fail_duplicateEmail() {
            // given
            UserRegisterRequest request = new UserRegisterRequest("test@monew.com", "tester", "password123");
            given(userRepository.existsByEmail(request.email())).willReturn(true);

            // when & then
            assertThatThrownBy(() -> userService.register(request))
                    .isInstanceOf(UserException.class)
                    .hasMessageContaining(UserErrorCode.EMAIL_ALREADY_EXISTS.getMessage());
        }
    }

    @Nested
    @DisplayName("로그인 테스트")
    class LoginTest {

        @Test
        @DisplayName("성공: 이메일과 비밀번호가 일치하면 로그인 성공")
        void success() {
            // given
            UserLoginRequest request = new UserLoginRequest("test@monew.com", "password123");
            User mockUser = createTestUser();
            given(userRepository.findByEmail(request.email())).willReturn(Optional.of(mockUser));

            // when
            UserDto result = userService.login(request);

            // then
            assertThat(result.email()).isEqualTo(request.email());
        }

        @Test
        @DisplayName("실패: 존재하지 않는 이메일이면 예외 발생")
        void fail_userNotFound() {
            // given
            UserLoginRequest request = new UserLoginRequest("wrong@monew.com", "password123");
            given(userRepository.findByEmail(request.email())).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> userService.login(request))
                    .isInstanceOf(UserException.class)
                    .hasMessageContaining(UserErrorCode.USER_NOT_FOUND.getMessage());
        }

        @Test
        @DisplayName("실패: 비밀번호가 일치하지 않으면 예외 발생")
        void fail_invalidPassword() {
            // given
            UserLoginRequest request = new UserLoginRequest("test@monew.com", "wrongpassword");
            User mockUser = createTestUser();
            given(userRepository.findByEmail(request.email())).willReturn(Optional.of(mockUser));

            // when & then
            assertThatThrownBy(() -> userService.login(request))
                    .isInstanceOf(UserException.class)
                    .hasMessageContaining(UserErrorCode.INVALID_PASSWORD.getMessage());
        }
    }

    @Nested
    @DisplayName("회원 정보 수정 테스트")
    class UpdateTest {

        @Test
        @DisplayName("성공: 닉네임 수정 성공")
        void success() {
            // given
            UUID userId = UUID.randomUUID();
            UserUpdateRequest request = new UserUpdateRequest("newNickname");
            User mockUser = createTestUser();
            given(userRepository.findById(userId)).willReturn(Optional.of(mockUser));

            // when
            UserDto result = userService.updateNickName(userId, request);

            // then
            assertThat(result.nickname()).isEqualTo("newNickname");
        }
    }

    @Nested
    @DisplayName("논리 삭제 테스트")
    class SoftDeleteTest {

        @Test
        @DisplayName("성공: 유저 논리 삭제 성공 (isDeleted 상태 변경)")
        void success() {
            // given
            UUID userId = UUID.randomUUID();
            User mockUser = createTestUser();
            given(userRepository.findById(userId)).willReturn(Optional.of(mockUser));

            // when
            userService.delete(userId);

            // then
            assertThat(mockUser.isDeleted()).isTrue();
            assertThat(mockUser.getDeletedAt()).isNotNull();
        }
    }

    @Nested
    @DisplayName("물리 삭제 테스트")
    class HardDeleteTest {

        @Test
        @DisplayName("성공: 유저 물리 삭제 쿼리 호출 확인")
        void success() {
            // given
            UUID userId = UUID.randomUUID();

            // when
            userService.hardDelete(userId);

            // then
            verify(userRepository).hardDeleteById(userId);
        }
    }
}