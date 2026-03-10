package com.donttaz.donttazapi.infra.banking.dto.response;

public record FinApiCreateDemandDepositAccountRec(
        String bankCode,
        String accountNo,
        FinApiCurrency currency
) {
}
