package com.donttaz.donttazapi.vault.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class DefaultVaultInterestTest {

    @Test
    @DisplayName("기본 이율은 4%로 고정되고 패널티는 0%에서 시작한다.")
    void vault_initial_rates() {
        DefaultVault vault = DefaultVault.builder().build();
        assertThat(vault.getBaseInterestRate()).isEqualTo(4.0);
        assertThat(vault.getPenaltyInterestRate()).isEqualTo(0.0);
    }

    @Test
    @DisplayName("출금 시 패널티가 0.1%씩 증가한다.")
    void withdraw_increases_penalty() {
        DefaultVault vault = DefaultVault.builder()
                .lockedAmount(100000L)
                .status(VaultStatus.LOCKED)
                .build();
        vault.withdraw(10000L);
        assertThat(vault.getPenaltyInterestRate()).isEqualTo(0.1);

        vault.withdraw(10000L);
        assertThat(vault.getPenaltyInterestRate()).isEqualTo(0.2);
    }

    @Test
    @DisplayName("한 달간 출금이 없으면 패널티가 0.1% 복구된다.")
    void penalty_recovery_logic() throws Exception {
        DefaultVault vault = DefaultVault.builder()
                .lockedAmount(100000L)
                .status(VaultStatus.LOCKED)
                .build();

        vault.withdraw(1000L);
        vault.withdraw(1000L);
        assertThat(vault.getPenaltyInterestRate()).isEqualTo(0.2);

        setPrivateField(vault, "lastWithdrawalAt", LocalDateTime.now().minusDays(45));
        setBaseEntityField(vault, "createdAt", LocalDateTime.now().minusDays(30));

        vault.calculateInterest();

        assertThat(vault.getPenaltyInterestRate()).isEqualTo(0.1);
    }

    private void setPrivateField(Object object, String fieldName, Object value) throws Exception {
        Field field = object.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(object, value);
    }

    private void setBaseEntityField(Object object, String fieldName, Object value) throws Exception {
        Field field = object.getClass().getSuperclass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(object, value);
    }
}
