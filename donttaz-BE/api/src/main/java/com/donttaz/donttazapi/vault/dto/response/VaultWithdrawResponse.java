package com.donttaz.donttazapi.vault.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
@Schema(description = "금고 긴급 출금 응답")
public record VaultWithdrawResponse(
                @Schema(description = "출금이 수행된 금고 ID", example = "10") Long vaultId,

                @Schema(description = "이번에 출금된 원금", example = "50000") Long withdrawnAmount,

                @Schema(description = "함께 지급된 이자 (패널티 적용 후)", example = "150") Long interestPaid,

                @Schema(description = "최종 내 계좌로 입금된 환급액 (원금+이자)", example = "50150") Long totalRefundAmount,

                @Schema(description = "누적 중도 출금 횟수 (위시리스트는 3회 제한)", example = "1") Integer withdrawalCount) {
}
