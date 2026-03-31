package com.donttaz.donttazapi.ai.dto.request;

import java.util.List;

public record AiPlanAnalysisRequest(
        String targetYearMonth,
        Integer monthlyIncome,
        Integer fixedExpense,
        Long currentBalance,
        Integer paydayDay,
        List<AiTransactionItem> accountTransactions,
        List<AiTransactionItem> cardTransactions
) {
    public record AiTransactionItem(
            String id,
            String transactionDate,
            Long amount,
            String counterpartyName,
            String category,
            String memo,
            String type
    ) {
    }
}
