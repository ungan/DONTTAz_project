package com.donttaz.donttazapi.vault.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.Builder;

@Builder
@Schema(description = "금고 목록 통합 응답")
public record VaultListResponse(
        @Schema(description = "활성화된 금고 목록 (기본 + 위시리스트)") List<VaultSummaryResponse> vaults) {
}
