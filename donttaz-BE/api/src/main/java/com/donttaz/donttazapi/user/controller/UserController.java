package com.donttaz.donttazapi.user.controller;

import com.donttaz.donttazapi.common.response.ApiResponse;
import com.donttaz.donttazapi.user.dto.request.CreateUserRequest;
import com.donttaz.donttazapi.user.dto.response.CreateUserResponse;
import com.donttaz.donttazapi.user.dto.response.UserSummaryResponse;
import com.donttaz.donttazapi.user.service.UserCommandService;
import com.donttaz.donttazapi.user.service.UserQueryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
