package com.donttaz.donttazapi.account.service.dto;

public record DemandDepositAccountCreateResult(
        String bankCode,
        String accountNo,
        String currency,
        String currencyName
) {
}
