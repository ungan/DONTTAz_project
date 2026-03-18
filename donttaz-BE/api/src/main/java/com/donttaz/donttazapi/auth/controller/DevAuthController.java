package com.donttaz.donttazapi.auth.controller;

import com.donttaz.donttazapi.auth.dto.response.LoginResponse;
import com.donttaz.donttazapi.auth.service.DevAuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@Profile({ "local", "dev" })
@RequiredArgsConstructor
public class DevAuthController {

    private final DevAuthService devAuthService;

    @PostMapping("/test-login")
    public LoginResponse testLogin() {
        return devAuthService.testLogin();
    }
}
