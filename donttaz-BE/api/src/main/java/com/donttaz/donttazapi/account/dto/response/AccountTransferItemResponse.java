package com.donttaz.donttazapi.account.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
@Schema(description = "계좌 이체 거래 결과 항목")
public record AccountTransferItemResponse(
        @Schema(description = "거래 고유 번호", example = "61")
        String transactionUniqueNo,

        @Schema(description = "거래 계좌번호", example = "0016174648358792")
        String accountNo,

        @Schema(description = "거래 일자", example = "20240401")
        String transactionDate,

        @Schema(description = "거래 유형 코드", example = "2")
        String transactionType,

        @Schema(description = "거래 유형명", example = "출금(이체)")
        String transactionTypeName,

        @Schema(description = "상대 계좌번호", example = "0204667768182760")
        String transactionAccountNo
) {
}
