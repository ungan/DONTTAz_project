package com.donttaz.donttazapi.vault.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

@Schema(description = "금고 긴급 출금 요청")
public record VaultWithdrawRequest(
        @Schema(description = "출금액", example = "50000") @NotNull(message = "출금액은 필수입니다.") @Positive(message = "출금액은 0보다 커야 합니다.") Long amount) {
}
