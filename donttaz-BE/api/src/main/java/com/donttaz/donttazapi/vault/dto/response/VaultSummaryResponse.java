package com.donttaz.donttazapi.vault.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
@Schema(description = "금고 요약 정보 응답")
public record VaultSummaryResponse(
        @Schema(description = "금고 ID", example = "1") Long vaultId,
        @Schema(description = "금고 종류 (DEFAULT: 기본 금고, WISHLIST: 목표 금고)", example = "DEFAULT") String vaultKind,
        @Schema(description = "지켜낸 돈 (잠긴 원금)", example = "150000") Long lockedAmount,

        @Schema(description = "금고 상태 (LOCKED: 잠금, MATURED: 만기, BROKEN: 해지)", example = "LOCKED") String status,

        @Schema(description = "금고 목표 이름 (목표 금고일 때만 반환)", example = "갤럭시북 구매", nullable = true) String wishName,

        @Schema(description = "목표 금액 (목표 금고일 때만 반환)", example = "2500000", nullable = true) Long goalAmount,

        @Schema(description = "기본 연 이자율 (DEFAULT: 4.0, WISHLIST: 6.0)", example = "4.0") Double baseInterestRate,

        @Schema(description = "현재 적용된 페널티 연 이자율", example = "0.1") Double penaltyInterestRate,
        @Schema(description = "중도 출금 횟수 (위시리스트는 최대 3회)", example = "1") Integer withdrawalCount) {
}
