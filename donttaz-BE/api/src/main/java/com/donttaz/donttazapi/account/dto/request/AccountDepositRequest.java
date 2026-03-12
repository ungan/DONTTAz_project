package com.donttaz.donttazapi.account.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

@Schema(description = "계좌 입금 요청")
public record AccountDepositRequest(
        @Schema(description = "입금 금액", example = "100000000")
        @NotBlank(message = "입금 금액은 필수입니다.")
        @Pattern(regexp = "^[1-9]\\d*$", message = "입금 금액은 0보다 큰 숫자여야 합니다.")
        String transactionBalance,

        @Schema(description = "입금 거래 요약", example = "(수시입출금) : 입금")
        @NotBlank(message = "입금 거래 요약은 필수입니다.")
        String transactionSummary
) {
}
