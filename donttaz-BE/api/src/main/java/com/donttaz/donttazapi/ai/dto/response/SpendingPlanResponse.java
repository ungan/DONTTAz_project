package com.donttaz.donttazapi.ai.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

public record SpendingPlanResponse(
        @Schema(description = "플랜 고유 ID", example = "305")
        Long planId,

        @Schema(description = "대상 연월", example = "2026-03")
        String targetYearMonth,

        @Schema(description = "산출된 총 예산", example = "2500000")
        Integer totalBudget,

        @Schema(description = "잉여 자금", example = "500000")
        Integer surplusAmount,

        @Schema(description = "카테고리별 예산 리스트")
        List<CategoryPlanItem> categoryPlans
) {
    public record CategoryPlanItem(
            @Schema(description = "카테고리명", example = "식비")
            String categoryName,

            @Schema(description = "할당 예산", example = "600000")
            Integer allocatedAmount,

            @Schema(description = "AI 코멘트", example = "지난달 배달비 지출이 높았어요. 이번 달은 60만 원으로 줄여보는 건 어떨까요?")
            String aiComment
    ) {
    }
}
