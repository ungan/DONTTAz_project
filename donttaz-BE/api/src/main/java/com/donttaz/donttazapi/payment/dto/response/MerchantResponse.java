package com.donttaz.donttazapi.payment.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
@Schema(description = "가맹점 응답")
public record MerchantResponse(
        @Schema(description = "가맹점 ID", example = "1")
        Long merchantId,

        @Schema(description = "가맹점명", example = "이마트")
        String name,

        @Schema(description = "정산 계좌번호", example = "0204667768182760")
        String settlementAccountNo,

        @Schema(description = "가맹점 카테고리", example = "MART")
        String category,

        @Schema(description = "활성 여부", example = "true")
        Boolean active
) {
}
