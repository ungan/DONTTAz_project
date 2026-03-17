package com.donttaz.donttazapi.vault.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.LocalDate;

@Schema(description = "위시리스트 목표 설정 요청")
public record SetWishlistGoalRequest(
        @Schema(description = "위시리스트 목표 이름", example = "갤럭시북 구매") @NotBlank(message = "목표 이름은 필수입니다.") String wishlistName,

        @Schema(description = "위시리스트 목표 금액", example = "2500000") @NotNull(message = "목표 금액은 필수입니다.") @Positive(message = "목표 금액은 0보다 커야 합니다.") Long goalAmount,

        @Schema(description = "위시리스트 목표 날짜 (YYYY-MM-DD)", example = "2026-12-31") LocalDate goalDate) {
}
