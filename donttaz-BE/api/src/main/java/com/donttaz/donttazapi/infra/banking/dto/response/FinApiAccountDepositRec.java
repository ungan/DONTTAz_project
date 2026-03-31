package com.donttaz.donttazapi.infra.banking.dto.response;

public record FinApiAccountDepositRec(
        String transactionUniqueNo,
        String transactionDate
) {
}
