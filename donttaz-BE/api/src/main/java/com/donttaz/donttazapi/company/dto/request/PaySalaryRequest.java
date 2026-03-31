package com.donttaz.donttazapi.company.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * 급여 지급 요청 데이터임.
 */
public record PaySalaryRequest(
        @Schema(description = "출금할 기업 계좌 ID", example = "1")
        @NotNull(message = "기업 계좌 ID 필수임.") Long companyAccountId,

        @Schema(description = "입금될 직원 계좌 번호", example = "001234567890")
        @NotBlank(message = "대상 계좌 번호 필수임.") String userAccountNo,

        @Schema(description = "이체 금액", example = "3000000")
        @NotNull(message = "금액 필수임.") @Positive(message = "금액은 0보다 커야 함.") Long amount) {
}
