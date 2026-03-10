package com.donttaz.donttazapi.account.service.dto;

public record AccountSummaryResult(
        String externalAccountId,
        String bankCode,
        String bankName,
        String accountNumber,
        String accountAlias,
        Integer balance
) {
}
