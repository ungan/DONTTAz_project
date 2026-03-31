package com.donttaz.donttazapi.vault.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.Builder;

@Builder
@Schema(description = "금고 거래 내역 응답")
public record VaultHistoryResponse(
                @Schema(description = "거래 ID", example = "1") Long id,
                @Schema(description = "연관된 금고 ID", example = "10") Long vaultId,
                @Schema(description = "거래 종류 (DEPOSIT: 입금/이자, WITHDRAW_BROKEN: 중도출금, WITHDRAW_MATURED: 만기출금)", example = "DEPOSIT") String transactionType,
                @Schema(description = "거래 금액 (원금 기준)", example = "50000") Long amount,
                @Schema(description = "함께 지급된 이자 (출금 시에만 발생)", example = "1200") Long interestPaid,
                @Schema(description = "거래 발생 일시", example = "2024-03-21T14:30:00") LocalDateTime createdAt) {
}
