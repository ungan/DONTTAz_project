package com.donttaz.donttazapi.infra.banking.dto.response;

public record FinApiTransactionItemResponse(
        String transactionUniqueNo,
        String transactionDate,
        String transactionTime,
        String transactionType,
        String transactionTypeName,
        String transactionAccountNo,
        String transactionBalance,
        String transactionAfterBalance,
        String transactionSummary,
        String transactionMemo
) {
}
