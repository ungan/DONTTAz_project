package com.donttaz.donttazapi.account.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
@Schema(description = "계좌 입금 결과")
public record AccountDepositResponse(
        @Schema(description = "거래 고유 번호", example = "59")
        String transactionUniqueNo,

        @Schema(description = "거래 일자", example = "20240401")
        String transactionDate
) {
}
