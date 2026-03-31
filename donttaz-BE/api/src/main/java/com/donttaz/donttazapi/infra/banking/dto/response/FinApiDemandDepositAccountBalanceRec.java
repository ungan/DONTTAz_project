package com.donttaz.donttazapi.infra.banking.dto.response;

public record FinApiDemandDepositAccountBalanceRec(
        String bankCode,
        String accountNo,
        String accountBalance,
        String accountCreatedDate,
        String accountExpiryDate,
        String lastTransactionDate,
        String currency
) {
}
