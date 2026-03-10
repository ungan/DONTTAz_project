package com.donttaz.donttazapi.account.service.dto;

public record DemandDepositProductResult(
        String accountTypeUniqueNo,
        String bankCode,
        String bankName,
        String accountTypeCode,
        String accountTypeName,
        String accountName,
        String accountDescription,
        String accountType
) {
}
