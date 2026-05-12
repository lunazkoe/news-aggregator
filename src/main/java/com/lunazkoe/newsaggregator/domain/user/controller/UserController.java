package com.lunazkoe.newsaggregator.domain.user.controller;

import com.lunazkoe.newsaggregator.domain.user.dto.request.UserLoginRequest;
import com.lunazkoe.newsaggregator.domain.user.dto.request.UserRegisterRequest;
import com.lunazkoe.newsaggregator.domain.user.dto.request.UserUpdateRequest;
import com.lunazkoe.newsaggregator.domain.user.dto.response.UserDto;
import com.lunazkoe.newsaggregator.domain.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "사용자 관리", description = "사용자 관련 API")
public class UserController {

    private final UserService userService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "회원가입", description = "새로운 사용자를 등록합니다.")
    public UserDto register(@Valid @RequestBody UserRegisterRequest request) {
        log.info("Received register request for email: {}", request.email());
        UserDto response = userService.register(request);
        return response;
    }

    @PostMapping("/login")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "로그인", description = "사용자 로그인을 처리합니다.")
    public UserDto login(@Valid @RequestBody UserLoginRequest request) {
        log.info("Received login request for emai: {}", request.email());
        UserDto response = userService.login(request);
        return response;
    }

    @DeleteMapping("/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "사용자 논리 삭제", description = "사용자를 논리적으로 삭제합니다.")
    public void deleteUser(@Parameter(description = "사용자 ID") @PathVariable("userId") UUID userId) {
        log.info("Received soft delete request for userId: {}", userId);
        userService.delete(userId);
    }

    @PatchMapping("/{userId}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "사용자 정보 수정", description = "사용자의 닉네임을 수정합니다.")
    public UserDto updateUserNickName(
            @Parameter(description = "사용자 ID") @PathVariable("userId") UUID userId,
            @Valid @RequestBody UserUpdateRequest request
    ) {
        log.info("Received update request for userId: {}", userId);
        UserDto response = userService.updateNickName(userId, request);
        return response;
    }

    @DeleteMapping("/{userId}/hard")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "사용자 물리 삭제", description = "사용자를 물리적으로 삭제합니다.")
    public void hardDeleteUser(@Parameter(description = "사용자 ID") @PathVariable("userId") UUID userId) {
        log.info("Received hard delete request for userId: {}", userId);
        userService.hardDelete(userId);
    }
}
