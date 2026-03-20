package com.donttaz.donttazapi.ai.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;

public record AiTransactionSyncRequest(
        @Schema(description = "수집 시작일", example = "2026-02-01")
        LocalDate startDate,

        @Schema(description = "수집 종료일", example = "2026-03-05")
        LocalDate endDate
) {
}
