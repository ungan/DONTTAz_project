package com.donttaz.donttazapi.ai.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

public record AiSpendingReportResponse(
        @Schema(description = "대상 연월", example = "2026-03")
        String yearMonth,

        @Schema(description = "봉인 금액", example = "300000")
        Integer sealedAmount,

        @Schema(description = "총 예산", example = "1500000")
        Integer totalBudget,

        @Schema(description = "총 지출액", example = "1943800")
        Integer totalSpent,

        @Schema(description = "전체 예산 달성률(%)", example = "129.6")
        Double achievementRate,

        @Schema(description = "소비 페르소나", example = "쇼핑족")
        String spendingPersona,

        @Schema(description = "AI 종합 피드백")
        String aiOverallFeedback,

        @Schema(description = "카테고리별 리포트")
        List<CategoryReportItem> categoryReports,

        @Schema(description = "개인화 피드백 목록")
        List<PersonalizedFeedbackItem> personalizedFeedback
) {
    public record CategoryReportItem(
            @Schema(description = "카테고리명", example = "기타")
            String categoryName,

            @Schema(description = "할당 예산", example = "750000")
            Integer allocatedAmount,

            @Schema(description = "실제 지출액", example = "900000")
            Integer spentAmount,

            @Schema(description = "월 전체 지출 대비 비중(%)", example = "46.3")
            Double categoryRate,

            @Schema(description = "카테고리 예산 달성률(%)", example = "120.0")
            Double budgetAchievementRate,

            @Schema(description = "카테고리별 피드백", example = "계획 대비 초과 소비입니다.")
            String feedback
    ) {
    }

    public record PersonalizedFeedbackItem(
            @Schema(description = "피드백 타입", example = "warning")
            String type,

            @Schema(description = "관련 카테고리명", example = "기타", nullable = true)
            String categoryName,

            @Schema(description = "피드백 문구", example = "기타가 계획 대비 150,000원 초과되었습니다.")
            String message
    ) {
    }
}
