package com.donttaz.donttazapi.account.service.dto;

public record TransferCommand(
        String depositAccountNo,
        String depositTransactionSummary,
        String transactionBalance,
        String withdrawalAccountNo,
        String withdrawalTransactionSummary
) {
}
