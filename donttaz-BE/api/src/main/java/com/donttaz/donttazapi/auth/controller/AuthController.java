package com.donttaz.donttazapi.auth.controller;

import com.donttaz.donttazapi.auth.dto.request.LoginRequest;
import com.donttaz.donttazapi.auth.dto.request.LogoutRequest;
import com.donttaz.donttazapi.auth.dto.response.LoginResponse;
import com.donttaz.donttazapi.auth.service.AuthService;
import com.donttaz.donttazapi.common.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return ApiResponse.success("로그인이 성공적으로 완료되었습니다.", response);
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
