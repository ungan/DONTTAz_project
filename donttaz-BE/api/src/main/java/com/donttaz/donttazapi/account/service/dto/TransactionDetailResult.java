package com.donttaz.donttazapi.account.service.dto;

import java.time.LocalDateTime;

public record TransactionDetailResult(
        String externalTransactionId,
        String externalAccountId,
        LocalDateTime transactionDate,
        String type,
        Integer amount,
        Integer balanceAfter,
        String counterpartyName,
        String counterpartyBank,
        String counterpartyAccount,
        Integer fee,
        String category,
        String memo
) {
}
