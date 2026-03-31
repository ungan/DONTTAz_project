package com.donttaz.donttazapi.infra.banking.dto.response;

public record FinApiDemandDepositAccountHolderNameRec(
        String bankCode,
        String bankName,
        String accountNo,
        String userName,
        String currency
) {
}
