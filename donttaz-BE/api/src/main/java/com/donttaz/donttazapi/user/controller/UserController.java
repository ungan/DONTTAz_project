package com.donttaz.donttazapi.user.controller;

import com.donttaz.donttazapi.common.response.ApiResponse;
import com.donttaz.donttazapi.user.dto.request.CreateUserRequest;
import com.donttaz.donttazapi.user.dto.request.FcmTokenRegisterRequest;
import com.donttaz.donttazapi.user.dto.request.UserUpdateRequest;
import com.donttaz.donttazapi.user.dto.request.UserWithdrawRequest;
import com.donttaz.donttazapi.user.dto.response.CreateUserResponse;
import com.donttaz.donttazapi.user.dto.response.UserSummaryResponse;
import com.donttaz.donttazapi.user.dto.response.UserUpdateResponse;
import com.donttaz.donttazapi.user.service.UserCommandService;
import com.donttaz.donttazapi.user.service.UserQueryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserCommandService userCommandService;
    private final UserQueryService userQueryService;

    @PostMapping("/signup")
    public ApiResponse<CreateUserResponse> signUp(@RequestBody @Valid CreateUserRequest request) {
        CreateUserResponse response = userCommandService.createUser(request);
        return ApiResponse.success("회원 가입에 성공했습니다.", response);
    }

    @GetMapping("/me")
    public ApiResponse<UserSummaryResponse> getMyInfo(@AuthenticationPrincipal User principal) {
        Long userId = Long.parseLong(principal.getUsername());
        UserSummaryResponse response = userQueryService.getUserSummary(userId);
        return ApiResponse.success("내 정보를 성공적으로 조회했습니다.", response);
    }

    @PostMapping("/fcm-token")
    public ApiResponse<Void> registerFcmToken(
            @AuthenticationPrincipal User principal,
            @RequestBody @Valid FcmTokenRegisterRequest request) {
        Long userId = Long.parseLong(principal.getUsername());
        userCommandService.registerFcmToken(userId, request);
        return ApiResponse.success("알림 토큰이 성공적으로 등록되었습니다.", null);
    }

    @PatchMapping("/me")
    public ApiResponse<UserUpdateResponse> updateMyInfo(
            @AuthenticationPrincipal User principal,
            @RequestBody @Valid UserUpdateRequest request) {
        Long userId = Long.parseLong(principal.getUsername());
        UserUpdateResponse response = userCommandService.updateUserInfo(userId, request);
        return ApiResponse.success("회원 정보가 성공적으로 수정되었습니다.", response);
    }

    @DeleteMapping("/me")
    public ApiResponse<Void> withdrawAccount(
            @AuthenticationPrincipal User principal,
            @RequestBody @Valid UserWithdrawRequest request) {
        Long userId = Long.parseLong(principal.getUsername());
        userCommandService.withdraw(userId, request);
        return ApiResponse.success("회원 탈퇴가 정상적으로 처리되었습니다.", null);
    }
}
