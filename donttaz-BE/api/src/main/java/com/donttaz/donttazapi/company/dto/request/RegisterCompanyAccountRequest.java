package com.donttaz.donttazapi.company.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 기업 계좌 등록 요청 데이터임.
 */
public record RegisterCompanyAccountRequest(
        @NotNull(message = "기업 ID 필수임.") Long companyId,

        @NotNull(message = "사용자 ID 필수임. (금융 API 연동용)") Long userId,

        @NotBlank(message = "계좌 번호 필수임.") String accountNumber) {
}
