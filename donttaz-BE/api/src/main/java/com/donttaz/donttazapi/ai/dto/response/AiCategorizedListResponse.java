package com.donttaz.donttazapi.ai.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.List;

public record AiCategorizedListResponse(
        @Schema(description = "전체 내역 수", example = "85")
        long totalElements,

        @Schema(description = "분류 내역 리스트")
        List<AiCategorizedTransactionItem> categorizedList
) {
    public record AiCategorizedTransactionItem(
            @Schema(description = "거래 ID", example = "5012")
            Long transactionId,

            @Schema(description = "결제 일시", example = "2026-03-04T19:30:00")
            LocalDateTime transactionDate,

            @Schema(description = "결제처", example = "요기요_치킨")
            String storeName,

            @Schema(description = "결제 금액", example = "23000")
            Long amount,

            @Schema(description = "추론 카테고리", example = "배달/외식")
            String aiCategory,

            @Schema(description = "확신도(신뢰도)", example = "0.95")
            Double confidenceScore,

            @Schema(description = "사용자 수정 여부", example = "false")
            Boolean isModified
    ) {
    }
}
