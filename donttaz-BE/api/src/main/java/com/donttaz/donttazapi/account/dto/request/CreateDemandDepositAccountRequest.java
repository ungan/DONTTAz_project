package com.donttaz.donttazapi.account.dto.request;

import jakarta.validation.constraints.NotBlank;

public record CreateDemandDepositAccountRequest(
        @NotBlank(message = "상품 식별자는 필수입니다.")
        String accountTypeUniqueNo
) {
}
