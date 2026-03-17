package com.donttaz.donttazapi.vault.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
@Schema(description = "금고 입금 응답")
public record VaultDepositResponse(
                @Schema(description = "금고 ID", example = "1") Long vaultId,

                @Schema(description = "입금액", example = "100000") Long depositedAmount,

                @Schema(description = "입금 후 금고 잔액", example = "100000") Long balanceAfter) {
}
