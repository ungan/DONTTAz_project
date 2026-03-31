package com.donttaz.donttazapi.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class PasswordResetRequest {

    @NotBlank(message = "재설정 토큰은 필수입니다.")
    private String resetToken;

    @NotBlank(message = "새 비밀번호는 필수입니다.")
    private String newPassword;
}
