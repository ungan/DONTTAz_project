package com.donttaz.donttazapi.payment.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
@Schema(description = "결제 응답")
public record PaymentResponse(
        @Schema(description = "결제 ID", example = "1")
        Long paymentId,

        @Schema(description = "가맹점 ID", example = "1")
        Long merchantId,

        @Schema(description = "가맹점명", example = "이마트")
        String merchantName,

        @Schema(description = "출금 계좌번호", example = "0016174648358792")
        String withdrawalAccountNo,

        @Schema(description = "정산 계좌번호", example = "0204667768182760")
        String settlementAccountNo,

        @Schema(description = "결제 금액", example = "10000")
        Long amount,

        @Schema(description = "출금 거래 고유번호", example = "61")
        String withdrawalTransactionId,

        @Schema(description = "입금 거래 고유번호", example = "62")
        String depositTransactionId,

        @Schema(description = "거래 일자", example = "20240401")
        String transactionDate,

        @Schema(description = "결제 상태", example = "COMPLETED")
        String status
) {
}
