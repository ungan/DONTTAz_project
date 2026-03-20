package com.donttaz.donttazapi.company.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * 급여 지급 요청 데이터임.
 */
public record PaySalaryRequest(
        @NotNull(message = "기업 계좌 ID 필수임.") Long companyAccountId,

        @NotBlank(message = "대상 계좌 번호 필수임.") String userAccountNo,

        @NotNull(message = "금액 필수임.") @Positive(message = "금액은 0보다 커야 함.") Long amount) {
}
