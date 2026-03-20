package com.donttaz.donttazapi.ai.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

public record TransactionCategoryUpdateRequest(
        @Schema(description = "변경할 카테고리", example = "식비")
        String updateCategory
) {
}
