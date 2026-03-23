package com.donttaz.donttazapi.company.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

/**
 * 기업 계좌 생성 요청 데이터임.
 */
public record RegisterCompanyAccountRequest(
                @Schema(description = "기업 ID", example = "1")
                @NotNull(message = "기업 ID 필수임.") Long companyId,

                @Schema(description = "기업 대표/관리자 사용자 ID (금융망 API 제약으로 인해 실제 개인 명의 연동 필수)", example = "1")
                @NotNull(message = "사용자 ID 필수임. (금융 API 연동용)") Long userId) {
}
