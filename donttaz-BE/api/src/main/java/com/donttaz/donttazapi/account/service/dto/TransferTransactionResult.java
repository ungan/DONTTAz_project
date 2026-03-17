package com.donttaz.donttazapi.account.service.dto;

public record TransferTransactionResult(
        String transactionUniqueNo,
        String accountNo,
        String transactionDate,
        String transactionType,
        String transactionTypeName,
        String transactionAccountNo
) {
}
