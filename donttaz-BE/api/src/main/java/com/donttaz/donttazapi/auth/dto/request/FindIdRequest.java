package com.donttaz.donttazapi.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class FindIdRequest {

    @NotBlank(message = "인증 식별자는 필수입니다.")
    private String identityVerificationId;
}
