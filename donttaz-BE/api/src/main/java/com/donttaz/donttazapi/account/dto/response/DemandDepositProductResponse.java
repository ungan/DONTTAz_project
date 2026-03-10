package com.donttaz.donttazapi.account.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
@Schema(description = "수시입출금 상품 정보")
public record DemandDepositProductResponse(
        @Schema(description = "상품 고유 식별자", example = "001-1-ffa4253081d540")
        String accountTypeUniqueNo,

        @Schema(description = "은행 코드", example = "001")
        String bankCode,

        @Schema(description = "은행 이름", example = "한국은행")
        String bankName,

        @Schema(description = "계좌 유형 코드", example = "1")
        String accountTypeCode,

        @Schema(description = "계좌 유형 이름", example = "수시입출금")
        String accountTypeName,

        @Schema(description = "상품명", example = "한국은행 수시입출금 상품명")
        String accountName,

        @Schema(description = "상품 설명", example = "한국은행 수시입출금 상품설명")
        String accountDescription,

        @Schema(description = "계좌 유형", example = "DOMESTIC")
        String accountType
) {
}
