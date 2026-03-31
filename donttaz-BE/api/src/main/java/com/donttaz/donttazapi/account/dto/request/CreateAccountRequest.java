package com.donttaz.donttazapi.account.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "수시입출금 계좌 생성 요청")
public record CreateAccountRequest(
        @Schema(
                description = "상품 조회 API에서 받은 수시입출금 상품 식별자",
                example = "001-1-ffa4253081d540"
        )
        @NotBlank(message = "상품 식별자는 필수입니다.")
        String accountTypeUniqueNo
) {
}
