package com.donttaz.donttazapi.vault.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
@Schema(description = "금고 요약 정보 응답")
public record VaultSummaryResponse(
        @Schema(description = "금고 ID", example = "1") Long vaultId,
        @Schema(description = "금고 종류", example = "DEFAULT") String vaultKind,
        @Schema(description = "지켜낸 돈 (잠긴 원금)", example = "150000") Long lockedAmount,

        @Schema(description = "금고 상태", example = "LOCKED") String status,

        @Schema(description = "금고 목표 이름", example = "갤럭시북 구매") String wishName,

        @Schema(description = "목표 금액", example = "500000") Long goalAmount,

        @Schema(description = "기본 연 이자율", example = "5.0") Double baseInterestRate,

        @Schema(description = "페널티 연 이자율", example = "0.1") Double penaltyInterestRate,
        @Schema(description = "중도 출금 횟수", example = "1") Integer withdrawalCount) {
}
