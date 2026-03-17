package com.donttaz.donttazapi.account.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

@Schema(description = "이체 한도 변경 요청")
public record AccountTransferLimitUpdateRequest(
        @Schema(description = "1회 이체 한도", example = "20000000")
        @NotBlank(message = "1회 이체 한도는 필수입니다.")
        @Pattern(regexp = "^[1-9]\\d*$", message = "1회 이체 한도는 0보다 큰 숫자여야 합니다.")
        String oneTimeTransferLimit,

        @Schema(description = "1일 이체 한도", example = "100000000")
        @NotBlank(message = "1일 이체 한도는 필수입니다.")
        @Pattern(regexp = "^[1-9]\\d*$", message = "1일 이체 한도는 0보다 큰 숫자여야 합니다.")
        String dailyTransferLimit
) {
}
