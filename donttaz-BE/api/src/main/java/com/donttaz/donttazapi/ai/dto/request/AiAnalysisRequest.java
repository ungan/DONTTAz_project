package com.donttaz.donttazapi.ai.dto.request;

import java.util.List;

public record AiAnalysisRequest(
        List<TransactionItem> transactions
) {
    public record TransactionItem(
            String transactionId,
            String transactionDate,
            Long amount,
            String counterpartyName,
            String type
    ) {
    }
}
