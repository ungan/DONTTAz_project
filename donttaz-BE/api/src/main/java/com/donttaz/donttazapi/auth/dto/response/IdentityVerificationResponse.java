package com.donttaz.donttazapi.auth.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class IdentityVerificationResponse {
    private String name;
    private String phoneNumber;
    private boolean isRegistered; // 기존 회원 여부
    private String maskedEmail;   // 기존 회원이면 이메일 안내
}
