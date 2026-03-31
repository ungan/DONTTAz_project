package com.donttaz.donttazapi.ai.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

public record SpendingPlanResponse(
        @Schema(description = "Plan ID", example = "305")
        Long planId,

        @Schema(description = "Target year-month", example = "2026-03")
        String targetYearMonth,

        @Schema(description = "Reference start month used by AI", example = "2025-12")
        String referenceStartMonth,

        @Schema(description = "Reference end month used by AI", example = "2026-02")
        String referenceEndMonth,

        @Schema(description = "Total monthly budget", example = "2500000")
        Integer totalBudget,

        @Schema(description = "Sealed amount suggested by AI", example = "750000")
        Integer sealedAmount,

        @Schema(description = "Remaining amount after budgeting", example = "500000")
        Integer surplusAmount,

        @Schema(description = "Configured payday day", example = "25")
        Integer paydayDay,

        @Schema(description = "Risk score suggested by AI", example = "0.12")
        Double riskScore,

        @Schema(description = "Budget strategy suggested by AI", example = "safe")
        String budgetStrategy,

        @Schema(description = "Recommended category budgets")
        List<CategoryPlanItem> categoryPlans,

        @Schema(description = "Personalized recommendations from AI")
        List<RecommendationItem> personalizedRecommendations,

        @Schema(description = "Detected spending persona", example = "여가충전형")
        String spendingPersona
) {
    public record CategoryPlanItem(
            @Schema(description = "Category name", example = "식비")
            String categoryName,

            @Schema(description = "Allocated amount", example = "600000")
            Integer allocatedAmount,

            @Schema(description = "AI comment for the category plan", example = "Recent food spending increased, so keep this category under control.")
            String aiComment
    ) {
    }

    public record RecommendationItem(
            @Schema(description = "Recommendation type", example = "warning")
            String type,

            @Schema(description = "Related category name", example = "여가")
            String categoryName,

            @Schema(description = "Recommendation message", example = "Entertainment spending is above the recommended ratio.")
            String message
    ) {
    }
}
