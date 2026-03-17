package com.donttaz.donttazapi.infra.banking.dto.response;

public record FinApiTransferRec(
        String transactionUniqueNo,
        String accountNo,
        String transactionDate,
        String transactionType,
        String transactionTypeName,
        String transactionAccountNo
) {
}
