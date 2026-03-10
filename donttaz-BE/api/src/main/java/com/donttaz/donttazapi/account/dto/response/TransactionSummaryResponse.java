package com.donttaz.donttazapi.account.dto.response;

import java.time.LocalDateTime;
import lombok.Builder;

@Builder
public record TransactionSummaryResponse(
        Long transactionId,
        LocalDateTime transactionDate,
        String type,
        Integer amount,
        Integer balanceAfter,
        String counterpartyName,
        String category
) {
}
