package com.donttaz.donttazapi.vault.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
@Schema(description = "금고 긴급 출금 응답")
public record VaultWithdrawResponse(
                @Schema(description = "금고 ID", example = "1") Long vaultId,

                @Schema(description = "출금액", example = "50000") Long withdrawnAmount,

                @Schema(description = "출금 후 금고 잔액", example = "50000") Long balanceAfter) {
}
