package com.donttaz.donttazapi.ai.dto.response;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record AiPlanAnalysisResponse(
        String status,
        String message,
        AiPlanData data
) {
    public AiPlanAnalysisResponse(Integer totalBudget, List<RecommendedCategoryPlan> categories) {
        this(
                "SUCCESS",
                null,
                new AiPlanData(null, null, null, totalBudget, null, null, null, null, categories, null, null, null)
        );
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record AiPlanData(
            String targetYearMonth,
            String referenceStartMonth,
            String referenceEndMonth,
            Integer totalBudget,
            Integer sealedAmount,
            Integer surplusAmount,
            Integer paydayDay,
            Double riskScore,
            @JsonProperty("categoryPlans")
            List<RecommendedCategoryPlan> categories,
            String budgetStrategy,
            List<PersonalizedRecommendation> personalizedRecommendations,
            String spendingPersona
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record RecommendedCategoryPlan(
            String categoryName,
            @JsonAlias({"allocatedAmount", "budgetAmount", "amount", "recommendedAmount"})
            Integer recommendedAmount,
            @JsonAlias({"comment", "aiComment"})
            String comment
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record PersonalizedRecommendation(
            String type,
            String categoryName,
            String message
    ) {
    }
}
