package com.donttaz.donttazapi.ai.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.util.List;

public record AiBurndownChartResponse(
        @Schema(description = "대상 연월", example = "2026-03")
        String targetYearMonth,

        @Schema(description = "총 할당 예산", example = "2500000")
        Integer totalBudget,

        @Schema(description = "현재 남은 예산", example = "1850000")
        Integer currentRemaining,

        @Schema(description = "생존 여부", example = "true")
        Boolean isSurviving,

        @Schema(description = "일자별 차트 데이터")
        List<BurndownChartItem> chartData
) {
    public record BurndownChartItem(
            @Schema(description = "해당 일자", example = "2026-03-05")
            LocalDate date,

            @Schema(description = "이상적 잔액", example = "2096774")
            Integer idealRemaining,

            @Schema(description = "실제 잔액", example = "1850000")
            Integer actualRemaining
    ) {
    }
}
