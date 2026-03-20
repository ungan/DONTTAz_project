package com.donttaz.donttazapi.account.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

@Schema(description = "1원 송금 인증코드 검증 요청")
public record OneWonVerificationConfirmRequest(
        @Schema(description = "1원 송금 인증 요청 ID", example = "7")
        @NotBlank(message = "인증 요청 ID는 필수입니다.")
        String verificationId,

        @Schema(description = "입력한 4자리 인증코드", example = "8212")
        @NotBlank(message = "인증코드는 필수입니다.")
        @Pattern(regexp = "^\\d{4}$", message = "인증코드는 4자리 숫자여야 합니다.")
        String authCode
) {
}
