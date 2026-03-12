package com.donttaz.donttazapi.account.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
@Schema(description = "계좌 출금 결과")
public record AccountWithdrawalResponse(
        @Schema(description = "거래 고유 번호", example = "60")
        String transactionUniqueNo,

        @Schema(description = "거래 일자", example = "20240401")
        String transactionDate
) {
}
