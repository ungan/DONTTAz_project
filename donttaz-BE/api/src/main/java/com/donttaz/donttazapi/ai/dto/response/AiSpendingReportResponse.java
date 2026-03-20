package com.donttaz.donttazapi.ai.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

public record AiSpendingReportResponse(
        @Schema(description = "대상 연월", example = "2026-02")
        String yearMonth,

        @Schema(description = "목표 총 예산", example = "2500000")
        Integer totalBudget,

        @Schema(description = "실제 총 지출액", example = "2650000")
        Integer totalSpent,

        @Schema(description = "전체 예산 달성률 (%)", example = "106")
        Integer achievementRate,

        @Schema(description = "AI 종합 피드백", example = "전체 예산을 15만 원 초과했어요. 특히 '배달/외식' 지출이 컸습니다.")
        String aiOverallFeedback,

        @Schema(description = "카테고리별 리포트")
        List<CategoryReportItem> categoryReports
) {
    public record CategoryReportItem(
            @Schema(description = "카테고리명", example = "배달/외식")
            String categoryName,

            @Schema(description = "할당 예산", example = "600000")
            Integer allocatedAmount,

            @Schema(description = "실제 지출액", example = "850000")
            Integer spentAmount,

            @Schema(description = "카테고리 달성률 (%)", example = "141")
            Integer categoryRate
    ) {
    }
}
