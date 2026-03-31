package com.donttaz.donttazapi.vault.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import com.donttaz.donttazapi.memorandum.dto.response.MemorandumResponse;

@Builder
@Schema(description = "금고 생성 응답")
public record CreateVaultResponse(
        @Schema(description = "생성된 금고 ID", example = "1") Long vaultId,

        @Schema(description = "생성된 금고 종류 (DEFAULT: 기본, WISHLIST: 목표)", example = "WISHLIST") String vaultKind,

        @Schema(description = "초기 지켜낸 돈 (잠긴 원금)", example = "0") Long lockedAmount,

        @Schema(description = "금고 상태 (초기 상태: LOCKED)", example = "LOCKED") String status,

        @Schema(description = "각서 정보 (위시리스트 금고일 때 포함)") com.donttaz.donttazapi.memorandum.dto.response.MemorandumResponse memorandum) {

    public static CreateVaultResponse fromDefault(com.donttaz.donttazapi.vault.domain.DefaultVault vault) {
        return CreateVaultResponse.builder()
                .vaultId(vault.getId())
                .vaultKind(com.donttaz.donttazapi.vault.domain.VaultKind.DEFAULT.name())
                .lockedAmount(vault.getLockedAmount())
                .status(vault.getStatus().name())
                .memorandum(null) // Default vaults do not have a memorandum
                .build();
    }

    public static CreateVaultResponse fromWishlist(com.donttaz.donttazapi.vault.domain.WishlistVault vault,
            MemorandumResponse memorandum) {
        return CreateVaultResponse.builder()
                .vaultId(vault.getId())
                .vaultKind(com.donttaz.donttazapi.vault.domain.VaultKind.WISHLIST.name())
                .lockedAmount(vault.getLockedAmount())
                .status(vault.getStatus().name())
                .memorandum(memorandum)
                .build();
    }
}
