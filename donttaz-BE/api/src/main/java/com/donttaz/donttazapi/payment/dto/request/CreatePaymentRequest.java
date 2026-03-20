package com.donttaz.donttazapi.payment.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

@Schema(description = "결제 요청")
public record CreatePaymentRequest(
        @Schema(description = "출금 계좌번호", example = "0016174648358792")
        @NotBlank(message = "출금 계좌번호는 필수입니다.")
        String withdrawalAccountNo,

        @Schema(description = "가맹점 ID", example = "1")
        @NotNull(message = "가맹점 ID는 필수입니다.")
        Long merchantId,

        @Schema(description = "결제 금액", example = "10000")
        @NotBlank(message = "결제 금액은 필수입니다.")
        @Pattern(regexp = "^[1-9]\\d*$", message = "결제 금액은 0보다 큰 숫자여야 합니다.")
        String amount
) {
}
