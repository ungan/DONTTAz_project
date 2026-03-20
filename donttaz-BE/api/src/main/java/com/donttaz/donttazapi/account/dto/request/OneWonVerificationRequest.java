package com.donttaz.donttazapi.account.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

@Schema(description = "1원 송금 인증 요청")
public record OneWonVerificationRequest(
        @Schema(description = "인증할 계좌번호", example = "0011214764051239")
        @NotBlank(message = "계좌번호는 필수입니다.")
        @Pattern(regexp = "^\\d{10,20}$", message = "계좌번호 형식이 올바르지 않습니다.")
        String accountNo
) {
}
