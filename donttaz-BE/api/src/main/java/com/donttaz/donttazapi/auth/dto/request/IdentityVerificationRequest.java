package com.donttaz.donttazapi.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class IdentityVerificationRequest {
    
    @NotBlank(message = "인증 ID는 필수입니다.")
    private String identityVerificationId;
}
