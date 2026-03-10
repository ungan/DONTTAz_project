package com.donttaz.donttazapi.account.dto.response;

import java.time.LocalDateTime;
import lombok.Builder;

@Builder
public record TransactionDetailResponse(
        Long transactionId,
        Long accountId,
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
