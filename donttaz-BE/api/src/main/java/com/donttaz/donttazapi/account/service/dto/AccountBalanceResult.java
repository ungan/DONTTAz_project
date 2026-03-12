package com.donttaz.donttazapi.account.service.dto;

public record AccountBalanceResult(
        String bankCode,
        String accountNo,
        String accountBalance,
        String accountCreatedDate,
        String accountExpiryDate,
        String lastTransactionDate,
        String currency
) {
}
