package com.donttaz.donttazapi.company.dto.request;

import jakarta.validation.constraints.NotBlank;

/**
 * 기업 정보 등록 요청 DTO임.
 */
public record CreateCompanyRequest(
        @NotBlank(message = "기업명 필수임.") String name) {
}
