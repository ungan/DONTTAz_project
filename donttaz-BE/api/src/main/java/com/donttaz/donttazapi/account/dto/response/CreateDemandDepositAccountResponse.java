package com.donttaz.donttazapi.account.dto.response;

import lombok.Builder;

@Builder
public record CreateDemandDepositAccountResponse(
        String bankCode,
        String accountNo,
        String currency,
        String currencyName
) {
}
