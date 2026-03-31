package com.donttaz.donttazapi.infra.banking.dto.response;

public record FinApiAccountWithdrawalRec(
        String transactionUniqueNo,
        String transactionDate
) {
}
