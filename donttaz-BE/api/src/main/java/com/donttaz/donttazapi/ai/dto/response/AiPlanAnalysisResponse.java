package com.donttaz.donttazapi.ai.dto.response;

import java.util.List;

public record AiPlanAnalysisResponse(
        Integer totalBudget,
        List<RecommendedCategoryPlan> categories
) {
    public record RecommendedCategoryPlan(
            String categoryName,
            Integer recommendedAmount,
            String comment
    ) {
    }
}
