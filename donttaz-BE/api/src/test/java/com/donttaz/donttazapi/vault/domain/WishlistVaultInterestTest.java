package com.donttaz.donttazapi.vault.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.offset;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.donttaz.donttazapi.common.exception.CustomException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class WishlistVaultInterestTest {

    @Test
    @DisplayName("위시리스트 출금 시 패널티가 0.3%p 증가하고 3회 제한이 적용된다.")
    void withdraw_wishlist_penalty_and_limit() {
        WishlistVault vault = WishlistVault.builder()
                .lockedAmount(100000L)
                .status(VaultStatus.LOCKED)
                .build();

        vault.withdraw(1000L);
        assertThat(vault.getPenaltyInterestRate()).isEqualTo(0.3);

        vault.withdraw(1000L);
        vault.withdraw(1000L);
        assertThat(vault.getWithdrawalCount()).isEqualTo(3);
        assertThat(vault.getPenaltyInterestRate()).isCloseTo(0.9, offset(0.0001));

        assertThrows(CustomException.class, () -> vault.withdraw(1000L));
    }

    @Test
    @DisplayName("중도 출금 없이 만기 시 원금의 1% 보너스가 지급된다.")
    void mature_bonus_success() {
        WishlistVault vault = WishlistVault.builder()
                .lockedAmount(1000000L) // 100만 원
                .status(VaultStatus.LOCKED)
                .build();

        vault.matureVault();

        assertThat(vault.getLockedAmount()).isEqualTo(1010000L); // 1.01M
        assertThat(vault.getStatus()).isEqualTo(VaultStatus.MATURED);
    }

    @Test
    @DisplayName("중도 출금이 한 번이라도 있으면 보너스가 지급되지 않는다.")
    void mature_no_bonus_if_withdrawn() {
        WishlistVault vault = WishlistVault.builder()
                .lockedAmount(1000000L)
                .status(VaultStatus.LOCKED)
                .build();

        vault.withdraw(10000L);
        vault.matureVault();

        assertThat(vault.getLockedAmount()).isEqualTo(990000L); // 1M - 10k
    }
}
