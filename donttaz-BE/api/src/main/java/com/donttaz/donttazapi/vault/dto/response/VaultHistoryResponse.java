package com.donttaz.donttazapi.vault.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.Builder;

@Builder
@Schema(description = "금고 거래 내역 응답")
public record VaultHistoryResponse(
        @Schema(description = "거래 ID", example = "1") Long id,
        @Schema(description = "금고 ID", example = "1") Long vaultId,
        @Schema(description = "거래 타입 (DEPOSIT, WITHDRAW_BROKEN, WITHDRAW_MATURED)", example = "DEPOSIT") String transactionType,
        @Schema(description = "거래 금액", example = "50000") Long amount,
        @Schema(description = "지급 이자", example = "0") Long interestPaid,
        @Schema(description = "거래 일시", example = "2024-03-18T14:30:00") LocalDateTime createdAt) {
}
