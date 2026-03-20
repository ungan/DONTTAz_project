package com.donttaz.donttazapi.vault.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.donttaz.donttazapi.common.exception.CustomException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class VaultDifferentiationTest {

    @Test
    @DisplayName("기본 금고는 잔액이 0원이 되어도 해지되지 않고 LOCKED 상태를 유지한다")
    void defaultVault_Withdrawal_DoesNotBreak() {
        // given
        Vault vault = Vault.builder()
                .vaultKind(VaultKind.DEFAULT)
                .lockedAmount(1000L)
                .status(VaultStatus.LOCKED)
                .build();

        // when
        vault.withdraw(1000L);

        // then
        assertThat(vault.getLockedAmount()).isEqualTo(0L);
        assertThat(vault.getStatus()).isEqualTo(VaultStatus.LOCKED);
    }

    @Test
    @DisplayName("위시리스트 금고는 잔액이 0원이 되면 BROKEN 상태로 자동 전환된다")
    void wishlistVault_Withdrawal_BreaksAtZero() {
        // given
        Vault vault = Vault.builder()
                .vaultKind(VaultKind.WISHLIST)
                .lockedAmount(1000L)
                .status(VaultStatus.LOCKED)
                .build();

        // when
        vault.withdraw(1000L);

        // then
        assertThat(vault.getLockedAmount()).isEqualTo(0L);
        assertThat(vault.getStatus()).isEqualTo(VaultStatus.BROKEN);
    }

    @Test
    @DisplayName("위시리스트 금고는 최대 3회까지만 부분 출금이 가능하며, 4회째는 예외가 발생한다")
    void wishlistVault_WithdrawalLimit_ThrowsException() {
        // given
        Vault vault = Vault.builder()
                .vaultKind(VaultKind.WISHLIST)
                .lockedAmount(10000L)
                .status(VaultStatus.LOCKED)
                .build();

        // when
        vault.withdraw(1000L); // 1회
        vault.withdraw(1000L); // 2회
        vault.withdraw(1000L); // 3회

        // then
        assertThat(vault.getWithdrawalCount()).isEqualTo(3);
        assertThatThrownBy(() -> vault.withdraw(1000L))
                .isInstanceOf(CustomException.class)
                .hasMessageContaining("최대 3회까지만");
    }

    @Test
    @DisplayName("위시리스트 금고는 출금 패널티가 0.3%p로 기본 금고(0.1%p)보다 높다")
    void wishlistVault_PenaltyWeight_IsHigher() {
        // given
        Vault defaultVault = Vault.builder()
                .vaultKind(VaultKind.DEFAULT)
                .lockedAmount(1000L)
                .build();
        Vault wishlistVault = Vault.builder()
                .vaultKind(VaultKind.WISHLIST)
                .lockedAmount(1000L)
                .build();

        // when
        defaultVault.withdraw(100L);
        wishlistVault.withdraw(100L);

        // then
        assertThat(defaultVault.getPenaltyInterestRate()).isEqualTo(0.1);
        assertThat(wishlistVault.getPenaltyInterestRate()).isEqualTo(0.3);
    }

    @Test
    @DisplayName("위시리스트 금고를 단 한 번의 출금 없이 만기하면 1% 보너스가 지급된다")
    void wishlistVault_MaturityBonus_Applied() {
        // given
        Vault vault = Vault.builder()
                .vaultKind(VaultKind.WISHLIST)
                .lockedAmount(1000000L) // 100만 원
                .status(VaultStatus.LOCKED)
                .build();

        // when
        vault.matureVault();

        // then
        // 100만 원 + (100만 원 * 0.01) = 101만 원
        assertThat(vault.getLockedAmount()).isEqualTo(1010000L);
        assertThat(vault.getStatus()).isEqualTo(VaultStatus.MATURED);
    }

    @Test
    @DisplayName("위시리스트 중도 출금 이력이 있으면 만기 보너스가 지급되지 않는다")
    void wishlistVault_MaturityBonus_NotApplied() {
        // given
        Vault vault = Vault.builder()
                .vaultKind(VaultKind.WISHLIST)
                .lockedAmount(1000000L)
                .status(VaultStatus.LOCKED)
                .build();

        // when
        vault.withdraw(100000L); // 한 번 출금
        vault.matureVault();

        // then
        // 보너스 없이 남은 잔액 90만 원 유지
        assertThat(vault.getLockedAmount()).isEqualTo(900000L);
    }
}
