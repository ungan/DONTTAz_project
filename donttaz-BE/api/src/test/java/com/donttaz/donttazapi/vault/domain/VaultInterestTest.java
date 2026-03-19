package com.donttaz.donttazapi.vault.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class VaultInterestTest {

    @Test
    @DisplayName("기본 이율은 4%로 고정되고 패널티는 0%에서 시작한다.")
    void vault_initial_rates() {
        Vault vault = Vault.builder().build();
        assertThat(vault.getBaseInterestRate()).isEqualTo(4.0);
        assertThat(vault.getPenaltyInterestRate()).isEqualTo(0.0);
    }

    @Test
    @DisplayName("출금 시 패널티가 0.1%씩 증가한다.")
    void withdraw_increases_penalty() {
        Vault vault = Vault.builder()
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
        Vault vault = Vault.builder()
                .lockedAmount(100000L)
                .status(VaultStatus.LOCKED)
                .build();

        // 초기 패널티 부여 (2번 출금하여 0.2%)
        vault.withdraw(1000L);
        vault.withdraw(1000L);
        assertThat(vault.getPenaltyInterestRate()).isEqualTo(0.2);

        // lastWithdrawalAt을 1.5개월 전으로 조작 (1달치 복구 대상)
        setPrivateField(vault, "lastWithdrawalAt", LocalDateTime.now().minusDays(45));

        // createdAt 설정 (이자 계산 시 필요)
        setBaseEntityField(vault, "createdAt", LocalDateTime.now().minusDays(30));

        // 로직 트리거 (이자 계산 시 복구 로직 수행)
        vault.calculateInterest();

        // 0.2% -> 0.1%로 복구되어야 함
        assertThat(vault.getPenaltyInterestRate()).isEqualTo(0.1);
    }

    @Test
    @DisplayName("보관 기간에 따른 이자 계산 검증 (4% 이율, 365일 보관 시 원금의 4%)")
    void calculate_interest_success() throws Exception {
        Vault vault = Vault.builder()
                .lockedAmount(1000000L) // 100만 원
                .status(VaultStatus.LOCKED)
                .build();

        // 생성일을 1년 전으로 조작
        setBaseEntityField(vault, "createdAt", LocalDateTime.now().minusDays(365));

        // 이자 계산 (4% 이율 시 40,000원 예상)
        Long interest = vault.calculateInterest();

        assertThat(interest).isEqualTo(40000L);
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
