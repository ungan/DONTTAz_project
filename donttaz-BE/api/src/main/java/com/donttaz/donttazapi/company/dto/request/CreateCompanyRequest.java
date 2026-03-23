package com.donttaz.donttazapi.company.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/**
 * 기업 정보 등록 요청 DTO임.
 */
public record CreateCompanyRequest(
        @Schema(description = "기업명", example = "SSAFY 주식회사")
        @NotBlank(message = "기업명 필수임.") String name) {
}
