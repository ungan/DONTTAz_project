package com.donttaz.donttazapi.auth.controller;

import com.donttaz.donttazapi.auth.dto.request.RefreshRequest;
import com.donttaz.donttazapi.auth.dto.request.FindIdRequest;
import com.donttaz.donttazapi.auth.dto.request.IdentityVerificationRequest;
import com.donttaz.donttazapi.auth.dto.request.LoginRequest;
import com.donttaz.donttazapi.auth.dto.request.LogoutRequest;
import com.donttaz.donttazapi.auth.dto.request.PasswordResetRequest;
import com.donttaz.donttazapi.auth.dto.request.PasswordVerifyIdentityRequest;
import com.donttaz.donttazapi.auth.dto.response.FindIdResponse;
import com.donttaz.donttazapi.auth.dto.response.IdentityVerificationResponse;
import com.donttaz.donttazapi.auth.dto.response.LoginResponse;
import com.donttaz.donttazapi.auth.dto.response.PasswordResetTokenResponse;
import com.donttaz.donttazapi.auth.service.AuthService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/find-id")
    public FindIdResponse findId(@Valid @RequestBody FindIdRequest request) {
        return authService.findId(request);
    }

    @PostMapping("/password/verify-identity")
    public PasswordResetTokenResponse verifyIdentityForPasswordReset(
            @Valid @RequestBody PasswordVerifyIdentityRequest request) {
        return authService.verifyIdentityForPasswordReset(request);
    }

    @PatchMapping("/password")
    public Void resetPassword(@Valid @RequestBody PasswordResetRequest request) {
        authService.resetPassword(request);
        return null;
    }

    @PostMapping("/verify-identity")
    public IdentityVerificationResponse verifyIdentity(@Valid @RequestBody IdentityVerificationRequest request) {
        return authService.verifyIdentity(request);
    }

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @PostMapping("/logout")
    public Void logout(
            @Valid @RequestBody LogoutRequest request,
            @AuthenticationPrincipal User principal) {
        Long userId = Long.parseLong(principal.getUsername());
        authService.logout(request, userId);
        return null;
    }

    @PostMapping("/refresh")
    public ApiResponse<LoginResponse> refresh(@Valid @RequestBody RefreshRequest request) {
        LoginResponse response = authService.refresh(request);
        return ApiResponse.success("토큰이 성공적으로 재발급되었습니다.", response);
    }
}
