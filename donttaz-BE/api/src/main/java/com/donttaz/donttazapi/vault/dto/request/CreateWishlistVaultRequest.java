package com.donttaz.donttazapi.vault.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.Valid;

@Schema(description = "위시리스트 금고 생성 요청")
public record CreateWishlistVaultRequest(
        @Schema(description = "위시리스트 목표 이름", example = "애플워치 구매") @NotBlank(message = "목표 이름은 필수입니다.") String wishName,

        @Schema(description = "목표 금액", example = "900000") @NotNull(message = "목표 금액은 필수입니다.") Long goalAmount,

        @Schema(description = "각서 정보") @Valid @NotNull(message = "각서 정보는 필수입니다.") com.donttaz.donttazapi.memorandum.dto.request.MemorandumRequest memorandum) {
}
