package com.donttaz.donttazapi.vault.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

@Schema(description = "금고 생성 요청")
public record CreateVaultRequest(
                @Schema(description = "금고 종류 (DEFAULT: 기본 금고, WISHLIST: 워시리스트 혹은 목표 금고)", example = "WISHLIST") 
                @NotNull(message = "금고 종류는 필수입니다. (DEFAULT 또는 WISHLIST)")
                @NotBlank(message = "금고 종류는 필수입니다. (DEFAULT 또는 WISHLIST)") String vaultKind,

                @Schema(description = "위시리스트 목표 이름 (WISHLIST일 경우 필수)", example = "제주도 여행") String wishlistName,

                @Schema(description = "위시리스트 목표 금액", example = "500000") Long goalAmount,

                @Schema(description = "위시리스트 목표 날짜 (YYYY-MM-DD)", example = "2026-12-31") LocalDate goalDate) {
}
