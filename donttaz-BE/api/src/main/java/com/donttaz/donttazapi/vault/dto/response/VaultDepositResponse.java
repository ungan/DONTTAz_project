package com.donttaz.donttazapi.vault.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
@Schema(description = "금고 입금 응답")
public record VaultDepositResponse(
                @Schema(description = "입금이 수행된 금고 ID", example = "1") Long vaultId,

                @Schema(description = "이번에 입금된 금액", example = "100000") Long depositedAmount,

                @Schema(description = "입금 후 총 지켜낸 돈 (잔액)", example = "250000") Long lockedAmount) {
}
