package com.donttaz.donttazapi.ai.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record AiSpendingReportGenerateResponse(
        String status,
        String message,
        ReportData data
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ReportData(
            String yearMonth,
            Integer sealedAmount,
            Integer totalBudget,
            Integer totalSpent,
            Double achievementRate,
            String spendingPersona,
            String aiOverallFeedback,
            List<CategoryReportItem> categoryReports,
            List<PersonalizedFeedbackItem> personalizedFeedback
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record CategoryReportItem(
            String categoryName,
            Integer allocatedAmount,
            Integer spentAmount,
            Double categoryRate,
            Double budgetAchievementRate,
            String feedback
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record PersonalizedFeedbackItem(
            String type,
            String categoryName,
            String message
    ) {
    }
}
