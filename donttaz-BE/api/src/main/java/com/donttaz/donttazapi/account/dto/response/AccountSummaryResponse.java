package com.donttaz.donttazapi.account.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
@Schema(description = "수시입출금 계좌 요약 정보")
public record AccountSummaryResponse(
        @Schema(description = "은행 코드", example = "020")
        String bankCode,

        @Schema(description = "은행명", example = "우리은행")
        String bankName,

        @Schema(description = "예금주명", example = "USER")
        String userName,

        @Schema(description = "마스킹된 계좌번호", example = "020-***-8182760")
        String accountNo,

        @Schema(description = "계좌 상품명", example = "우리은행 수시입출금 상품명")
        String accountName,

        @Schema(description = "계좌 유형 코드", example = "1")
        String accountTypeCode,

        @Schema(description = "계좌 유형명", example = "수시입출금")
        String accountTypeName,

        @Schema(description = "계좌 생성일", example = "20240320")
        String accountCreatedDate,

        @Schema(description = "계좌 만기일", example = "20290320")
        String accountExpiryDate,

        @Schema(description = "1일 이체 한도", example = "100000000")
        String dailyTransferLimit,

        @Schema(description = "1회 이체 한도", example = "20000000")
        String oneTimeTransferLimit,

        @Schema(description = "현재 잔액", example = "8003477")
        String accountBalance,

        @Schema(description = "마지막 거래일", example = "20240323")
        String lastTransactionDate,

        @Schema(description = "통화 코드", example = "KRW")
        String currency,

        @Schema(description = "대표계좌 여부", example = "true")
        Boolean isPrimary
) {
}
