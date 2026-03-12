package com.donttaz.donttazapi.account.service.dto;

public record AccountHolderNameResult(
        String bankCode,
        String bankName,
        String accountNo,
        String userName,
        String currency
) {
}
