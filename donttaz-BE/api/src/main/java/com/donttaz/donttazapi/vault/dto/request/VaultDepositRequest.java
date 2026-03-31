package com.donttaz.donttazapi.vault.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

@Schema(description = "금고 입금 요청")
public record VaultDepositRequest(
        @Schema(description = "입금액", example = "100000") @NotNull(message = "입금액은 필수입니다.") @Positive(message = "입금액은 0보다 커야 합니다.") Long amount) {
}