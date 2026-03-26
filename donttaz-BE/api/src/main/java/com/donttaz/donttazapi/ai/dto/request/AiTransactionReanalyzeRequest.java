package com.donttaz.donttazapi.ai.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;

public record AiTransactionReanalyzeRequest(
        @Schema(description = "재분류 시작일", example = "2025-12-01")
        LocalDate startDate,

        @Schema(description = "재분류 종료일", example = "2026-03-01")
        LocalDate endDate,

        @Schema(description = "기존 AI 메타데이터가 있어도 강제로 재분류할지 여부", example = "false")
        Boolean force
) {
}
