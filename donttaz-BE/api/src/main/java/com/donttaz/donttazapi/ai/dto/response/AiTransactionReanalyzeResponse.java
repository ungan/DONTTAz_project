package com.donttaz.donttazapi.ai.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record AiTransactionReanalyzeResponse(
        @Schema(description = "재분류 조회 시작일", example = "2025-12-01")
        LocalDate startDate,

        @Schema(description = "재분류 조회 종료일", example = "2026-03-01")
        LocalDate endDate,

        @Schema(description = "조회된 거래 수", example = "45")
        int matchedTransactionCount,

        @Schema(description = "실제로 AI 재분류 요청을 보낸 거래 수", example = "12")
        int reanalyzedCount,

        @Schema(description = "강제 재분류 여부", example = "false")
        boolean force,

        @Schema(description = "최종 재분류 시각", example = "2026-03-25T14:30:00")
        LocalDateTime lastAnalyzedTime
) {
}
