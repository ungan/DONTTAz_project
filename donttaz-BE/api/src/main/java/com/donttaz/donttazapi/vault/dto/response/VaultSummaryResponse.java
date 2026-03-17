package com.donttaz.donttazapi.vault.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import lombok.Builder;

@Builder
@Schema(description = "금고 요약 정보 응답")
public record VaultSummaryResponse(
                @Schema(description = "금고 ID", example = "1") Long vaultId,

                @Schema(description = "금고 종류 (DEFAULT / WISHLIST)", example = "WISHLIST") String vaultKind,

                @Schema(description = "가상 잔액", example = "150000") Long balance,

                @Schema(description = "금고 상태", example = "LOCKED") String status,

                @Schema(description = "위시리스트 목표 이름 (WISHLIST 전용)", example = "제주도 여행") String wishlistName,

                @Schema(description = "위시리스트 목표 금액 (WISHLIST 전용)", example = "500000") Long goalAmount,

                @Schema(description = "위시리스트 목표 날짜 (WISHLIST 전용)", example = "2026-12-31") LocalDate goalDate,

                @Schema(description = "위시리스트 목표 달성 여부 (WISHLIST 전용)", example = "false") Boolean isAchieved) {
}
