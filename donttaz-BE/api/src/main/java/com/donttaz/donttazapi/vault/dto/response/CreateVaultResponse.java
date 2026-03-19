package com.donttaz.donttazapi.vault.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
@Schema(description = "금고 생성 응답")
public record CreateVaultResponse(
        @Schema(description = "금고 ID", example = "1") Long vaultId,

        @Schema(description = "지켜낸 돈 (잠긴 원금)", example = "0") Long lockedAmount,

        @Schema(description = "금고 상태", example = "LOCKED") String status) {
}
