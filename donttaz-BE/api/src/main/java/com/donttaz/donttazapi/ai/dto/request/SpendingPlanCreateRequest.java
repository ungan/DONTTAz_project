package com.donttaz.donttazapi.ai.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

public record SpendingPlanCreateRequest(
        @Schema(description = "목표 연월 (YYYY-MM)", example = "2026-03")
        String targetYearMonth,

        @Schema(description = "예상 월 수입", example = "3000000")
        Integer monthlyIncome
) {
}
