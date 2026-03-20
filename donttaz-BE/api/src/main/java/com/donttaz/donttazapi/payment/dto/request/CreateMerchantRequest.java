package com.donttaz.donttazapi.payment.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "가맹점 생성 요청")
public record CreateMerchantRequest(
        @Schema(description = "가맹점명", example = "이마트")
        @NotBlank(message = "가맹점명은 필수입니다.")
        String name,

        @Schema(description = "정산 계좌번호", example = "0204667768182760")
        @NotBlank(message = "정산 계좌번호는 필수입니다.")
        String settlementAccountNo,

        @Schema(description = "가맹점 카테고리", example = "MART")
        String category
) {
}
