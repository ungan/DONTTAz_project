package com.donttaz.donttazapi.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class LogoutRequest {
    @NotBlank(message = "갱신 토큰은 필수 입력 값입니다.")
    private String refreshToken;
}
