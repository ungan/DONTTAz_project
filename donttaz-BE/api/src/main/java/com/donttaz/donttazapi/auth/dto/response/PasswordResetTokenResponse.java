package com.donttaz.donttazapi.auth.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PasswordResetTokenResponse {
    private String resetToken;
}
