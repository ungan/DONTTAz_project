package com.donttaz.donttazapi.vault.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
@Schema(description = "금고 생성 응답")
public record CreateVaultResponse(
                @Schema(description = "생성된 금고 ID", example = "1") Long vaultId,

                @Schema(description = "생성된 금고 종류 (DEFAULT: 기본, WISHLIST: 목표)", example = "WISHLIST") String vaultKind,

                @Schema(description = "초기 지켜낸 돈 (잠긴 원금)", example = "0") Long lockedAmount,

                @Schema(description = "금고 상태 (초기 상태: LOCKED)", example = "LOCKED") String status) {
}
