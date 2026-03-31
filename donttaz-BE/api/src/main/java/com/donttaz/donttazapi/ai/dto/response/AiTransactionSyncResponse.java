package com.donttaz.donttazapi.ai.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

public record AiTransactionSyncResponse(
        @Schema(description = "신규 수집 건수", example = "45")
        int syncedCount,

        @Schema(description = "최종 수집 일시", example = "2026-03-05T10:15:00")
        LocalDateTime lastSyncTime
) {
}
