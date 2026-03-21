package com.donttaz.donttazapi.vault.dto.request;

import com.donttaz.donttazapi.vault.domain.VaultKind;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "금고 생성 요청")
public record CreateVaultRequest(
        @Schema(description = "금고 종류 (DEFAULT: 기본 금고, WISHLIST: 목표 금고)", example = "WISHLIST") @NotNull(message = "금고 종류는 필수입니다.") VaultKind vaultKind,

        @Schema(description = "위시리스트 목표 이름 (목표 금고일 때 필수)", example = "애플워치 구매") String wishName,

        @Schema(description = "목표 금액 (목표 금고일 때 필수)", example = "900000") Long goalAmount,

        @Schema(description = "각서 정보 (위시리스트 금고일 때 필수)") com.donttaz.donttazapi.memorandum.dto.request.MemorandumRequest memorandum) {
}
