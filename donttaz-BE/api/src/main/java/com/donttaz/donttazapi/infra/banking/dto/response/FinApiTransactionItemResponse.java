package com.donttaz.donttazapi.infra.banking.dto.response;

import java.time.LocalDateTime;

public record FinApiTransactionItemResponse(
        String transactionId,
        LocalDateTime transactionDate,
        String transactionType,
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
