package com.donttaz.donttazapi.account.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
@Schema(description = "이체 한도 변경 결과")
public record AccountTransferLimitUpdateResponse(
        @Schema(description = "은행 코드", example = "001")
        String bankCode,

        @Schema(description = "은행명", example = "한국은행")
        String bankName,

        @Schema(description = "계좌번호", example = "0016174648358792")
        String accountNo,

        @Schema(description = "계좌 상품명", example = "한국은행 수시입출금 상품명")
        String accountName,

        @Schema(description = "1일 이체 한도", example = "100000000")
        String dailyTransferLimit,

        @Schema(description = "1회 이체 한도", example = "20000000")
        String oneTimeTransferLimit,

        @Schema(description = "계좌 잔액", example = "89900000")
        String accountBalance,

        @Schema(description = "마지막 거래일", example = "20240401")
        String lastTransactionDate,

        @Schema(description = "통화 코드", example = "KRW")
        String currency
) {
}
