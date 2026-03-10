package com.donttaz.donttazapi.infra.banking.dto.response;

public record FinApiDemandDepositProductRec(
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
