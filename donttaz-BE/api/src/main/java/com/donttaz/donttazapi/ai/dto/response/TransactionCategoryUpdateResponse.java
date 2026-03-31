package com.donttaz.donttazapi.ai.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

public record TransactionCategoryUpdateResponse(
        @Schema(description = "거래 ID", example = "5012")
        Long transactionId,

        @Schema(description = "이전 카테고리", example = "편의점")
        String previousCategory,

        @Schema(description = "수정된 카테고리", example = "식비")
        String updatedCategory,

        @Schema(description = "사용자 수정 여부", example = "true")
        Boolean isModified,

        @Schema(description = "수정 일시", example = "2026-03-05T10:45:00")
        LocalDateTime modifiedAt
) {
}
