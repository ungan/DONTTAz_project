package com.donttaz.donttazapi.auth.controller;

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
import com.donttaz.donttazapi.common.response.ApiResponse;
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
    public ApiResponse<FindIdResponse> findId(@Valid @RequestBody FindIdRequest request) {
        FindIdResponse response = authService.findId(request);
        return ApiResponse.success("아이디 찾기에 성공했습니다.", response);
    }

    @PostMapping("/password/verify-identity")
    public ApiResponse<PasswordResetTokenResponse> verifyIdentityForPasswordReset(@Valid @RequestBody PasswordVerifyIdentityRequest request) {
        PasswordResetTokenResponse response = authService.verifyIdentityForPasswordReset(request);
        return ApiResponse.success("본인인증이 완료되었습니다. 비밀번호를 재설정해 주세요.", response);
    }

    @PatchMapping("/password")
    public ApiResponse<Void> resetPassword(@Valid @RequestBody PasswordResetRequest request) {
        authService.resetPassword(request);
        return ApiResponse.success("비밀번호가 성공적으로 변경되었습니다.", null);
    }

    @PostMapping("/verify-identity")
    public ApiResponse<IdentityVerificationResponse> verifyIdentity(@Valid @RequestBody IdentityVerificationRequest request) {
        IdentityVerificationResponse response = authService.verifyIdentity(request);
        return ApiResponse.success("본인인증이 성공적으로 검증되었습니다.", response);
    }

    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return ApiResponse.success("로그인이 성공적으로 완료되었습니다.", response);
    }

    @org.springframework.context.annotation.Profile({"local", "dev"})
    @PostMapping("/test-login")
    public ApiResponse<LoginResponse> testLogin() {
        LoginResponse response = authService.testLogin();
        return ApiResponse.success("테스트 계정으로 자동 로그인 되었습니다.", response);
    }

    @PostMapping("/logout")
    public ApiResponse<Void> logout(
            @Valid @RequestBody LogoutRequest request,
            @AuthenticationPrincipal User principal) {
        Long userId = Long.parseLong(principal.getUsername());
        authService.logout(request, userId);
        return ApiResponse.success("안전하게 로그아웃 되었습니다.", null);
    }
}
