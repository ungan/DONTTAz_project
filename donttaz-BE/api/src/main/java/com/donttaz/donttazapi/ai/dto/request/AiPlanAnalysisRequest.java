package com.donttaz.donttazapi.ai.dto.request;

import java.util.List;

public record AiPlanAnalysisRequest(
        Integer monthlyIncome,
        List<CategorySummary> history
) {
    public record CategorySummary(
            String category,
            Long totalAmount
    ) {
    }
}
