package com.donttaz.donttazapi.vault.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
@Schema(description = "금고 긴급 출금 응답")
public record VaultWithdrawResponse(
        @Schema(description = "금고 ID", example = "1") Long vaultId,

        @Schema(description = "출금액 (원금)", example = "50000") Long withdrawnAmount,

        @Schema(description = "지급 이자 (페널티/정상)", example = "15") Long interestPaid,

        @Schema(description = "최종 환급액 (원금+이자)", example = "50015") Long totalRefundAmount) {
}
