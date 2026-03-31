package com.donttaz.donttazapi.ai.dto.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record AiSpendingReportGenerateRequest(
        String yearMonth,
        Integer paydayDay,
        ReportPlanSummary plan,
        List<AiTransactionItem> cardTransactions,
        List<AiTransactionItem> accountTransactions
) {
    public record ReportPlanSummary(
            Integer totalBudget,
            Integer sealedAmount,
            List<CategoryPlanItem> categoryPlans
    ) {
    }

    public record CategoryPlanItem(
            String categoryName,
            Integer allocatedAmount,
            String aiComment
    ) {
    }

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
