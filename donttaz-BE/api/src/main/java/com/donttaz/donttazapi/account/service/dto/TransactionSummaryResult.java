package com.donttaz.donttazapi.account.service.dto;

import java.time.LocalDateTime;

public record TransactionSummaryResult(
        String externalTransactionId,
        LocalDateTime transactionDate,
        String type,
        Integer amount,
        Integer balanceAfter,
        String counterpartyName,
        String category
) {
}
