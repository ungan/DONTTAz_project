package com.donttaz.donttazapi.company.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 기업 정보 등록 요청 DTO임.
 */
public record CreateCompanyRequest(
        @Schema(description = "기업명", example = "SSAFY 주식회사")
        @NotBlank(message = "기업명 필수임.") String name,

        @Schema(description = "기업 대표/관리자 사용자 ID (금융망 API 제약 상 개인 명의 연동 필수)", example = "100")
        @NotNull(message = "사용자 ID 필수임.") Long userId) {
}
