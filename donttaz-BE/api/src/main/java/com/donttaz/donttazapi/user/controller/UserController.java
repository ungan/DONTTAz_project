package com.donttaz.donttazapi.user.controller;

import com.donttaz.donttazapi.user.dto.request.CreateUserRequest;
import com.donttaz.donttazapi.user.dto.request.FcmTokenRegisterRequest;
import com.donttaz.donttazapi.user.dto.request.UserUpdateRequest;
import com.donttaz.donttazapi.user.dto.request.UserWithdrawRequest;
import com.donttaz.donttazapi.user.dto.response.CreateUserResponse;
import com.donttaz.donttazapi.user.dto.response.UserSummaryResponse;
import com.donttaz.donttazapi.user.dto.response.UserUpdateResponse;
import com.donttaz.donttazapi.user.service.UserCommandService;
import com.donttaz.donttazapi.user.service.UserQueryService;
import com.donttaz.donttazapi.common.response.ApiResponse;
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
    public CreateUserResponse signUp(@RequestBody @Valid CreateUserRequest request) {
        return userCommandService.createUser(request);
    }

    @GetMapping("/me")
    public UserSummaryResponse getMyInfo(@AuthenticationPrincipal User principal) {
        Long userId = Long.parseLong(principal.getUsername());
        return userQueryService.getUserSummary(userId);
    }

    @PostMapping("/fcm-token")
    public void registerFcmToken(
            @AuthenticationPrincipal User principal,
            @RequestBody @Valid FcmTokenRegisterRequest request) {
        Long userId = Long.parseLong(principal.getUsername());
        userCommandService.registerFcmToken(userId, request);
    }

    @PatchMapping("/me")
    public UserUpdateResponse updateMyInfo(
            @AuthenticationPrincipal User principal,
            @RequestBody @Valid UserUpdateRequest request) {
        Long userId = Long.parseLong(principal.getUsername());
        return userCommandService.updateUserInfo(userId, request);
    }

    @DeleteMapping("/me")
    public void withdrawAccount(
            @AuthenticationPrincipal User principal,
            @RequestBody @Valid UserWithdrawRequest request) {
        Long userId = Long.parseLong(principal.getUsername());
        userCommandService.withdraw(userId, request);
    }
}
