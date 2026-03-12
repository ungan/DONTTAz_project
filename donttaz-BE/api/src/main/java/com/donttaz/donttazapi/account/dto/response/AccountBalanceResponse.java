package com.donttaz.donttazapi.account.dto.response;

import lombok.Builder;

@Builder
public record AccountBalanceResponse(
        String bankCode,
        String accountNo,
        String accountBalance,
        String accountCreatedDate,
        String accountExpiryDate,
        String lastTransactionDate,
        String currency
) {
}
