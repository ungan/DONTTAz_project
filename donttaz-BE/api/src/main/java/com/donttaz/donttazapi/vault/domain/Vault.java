package com.donttaz.donttazapi.vault.domain;

import com.donttaz.donttazapi.common.domain.BaseEntity;
import com.donttaz.donttazapi.common.exception.CustomException;
import com.donttaz.donttazapi.common.exception.ErrorCode;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "vault", indexes = {
        @Index(name = "idx_vault_user_status", columnList = "user_id, status")
})
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Vault extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "vault_id")
    private Long vaultId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "vault_kind", nullable = false, length = 20)
    private VaultKind vaultKind;

    @Column(name = "wish_name", length = 100)
    private String wishName;

    @Column(name = "goal_amount")
    private Long goalAmount;

    @Column(name = "target_date")
    private LocalDate targetDate;

    @Column(name = "locked_amount", nullable = false)
    private Long lockedAmount;

    @Column(name = "base_interest_rate", nullable = false)
    private Double baseInterestRate;

    @Column(name = "penalty_interest_rate", nullable = false)
    private Double penaltyInterestRate;

    @Column(name = "last_withdrawal_at")
    private LocalDateTime lastWithdrawalAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private VaultStatus status;

    @Builder
    private Vault(Long userId, VaultKind vaultKind, String wishName, Long goalAmount,
            LocalDate targetDate, Long lockedAmount, Double baseInterestRate,
            Double penaltyInterestRate, VaultStatus status) {
        this.userId = userId;
        this.vaultKind = vaultKind != null ? vaultKind : VaultKind.DEFAULT;
        this.wishName = wishName;
        this.goalAmount = goalAmount;
        this.targetDate = targetDate;
        this.lockedAmount = lockedAmount != null ? lockedAmount : 0L;
        this.baseInterestRate = 4.0; // 4% 고정
        this.penaltyInterestRate = penaltyInterestRate != null ? penaltyInterestRate : 0.0;
        this.status = status != null ? status : VaultStatus.LOCKED;
        this.lastWithdrawalAt = LocalDateTime.now(); // 초기값은 생성 시점
    }

    public void deposit(Long amount) {
        if (amount == null || amount <= 0) {
            throw new CustomException(ErrorCode.INVALID_REQUEST, "입금액은 0보다 커야 합니다.");
        }
        this.lockedAmount += amount;
    }

    /**
     * 일일 이자를 잔액에 가산합니다. (배치 작업용)
     */
    public void addInterest(Long interestAmount) {
        if (interestAmount != null && interestAmount > 0) {
            this.lockedAmount += interestAmount;
        }
    }

    public void updateGoal(String wishName, Long goalAmount, LocalDate targetDate) {
        if (this.status != VaultStatus.LOCKED) {
            throw new CustomException(ErrorCode.INVALID_REQUEST, "잠금 상태인 금고만 목표를 수정할 수 있습니다.");
        }
        this.wishName = wishName;
        this.goalAmount = goalAmount;
        this.targetDate = targetDate;
    }

    public void breakVault() {
        if (this.status != VaultStatus.LOCKED) {
            throw new CustomException(ErrorCode.INVALID_REQUEST, "이미 해지된 금고입니다.");
        }
        this.status = VaultStatus.BROKEN;
    }

    /**
     * 긴급 출금 로직 (부분 출금 허용 및 패널티 누적/복구 반영)
     */
    public void withdraw(Long amount) {
        if (this.status != VaultStatus.LOCKED) {
            throw new CustomException(ErrorCode.INVALID_REQUEST, "잠금 상태인 금고만 출금할 수 있습니다.");
        }
        if (amount == null || amount <= 0 || amount > this.lockedAmount) {
            throw new CustomException(ErrorCode.INVALID_REQUEST, "출금액이 올바르지 않거나 잔액이 부족합니다.");
        }

        // 1. 복구 로직 먼저 적용 (연속 미출금 달 수만큼 패널티 차감)
        applyMonthlyRecovery();

        // 2. 출금 처리
        this.lockedAmount -= amount;

        // 3. 패널티 누적 (0.1% 증가) 및 최종 출금 시간 업데이트
        this.penaltyInterestRate += 0.1;
        this.lastWithdrawalAt = LocalDateTime.now();

        // 잔액이 0원이 되면 자동으로 해지 상태로 변경 (선택 사항)
        if (this.lockedAmount == 0) {
            this.status = VaultStatus.BROKEN;
        }
    }

    /**
     * 현재의 패널티와 보관 기간을 고려하여 지급할 이자를 계산합니다. 이자 = 잔액 * (기본 - 패널티) / 100 / 365 * 보관일수
     */
    public Long calculateInterest() {
        // 이자 계산 전 복구분 우선 반영
        applyMonthlyRecovery();

        double currentEffectiveRate = this.baseInterestRate - this.penaltyInterestRate;
        if (currentEffectiveRate < 0)
            currentEffectiveRate = 0;

        // 보관 일수 계산 (생성일로부터 현재까지)
        LocalDateTime created = this.getCreatedAt() != null ? this.getCreatedAt() : LocalDateTime.now();
        long days = ChronoUnit.DAYS.between(created.toLocalDate(), LocalDate.now());
        if (days <= 0)
            return 0L;

        // 보관 일수 반영 시 '일일 이자' 개념 적용
        double dailyRate = currentEffectiveRate / 100.0 / 365.0;
        return (long) (this.lockedAmount * dailyRate * days);
    }

    /**
     * 하루치 이자를 계산합니다. (배치 작업용)
     */
    public Long calculateDailyInterest() {
        applyMonthlyRecovery();

        double currentEffectiveRate = this.baseInterestRate - this.penaltyInterestRate;
        if (currentEffectiveRate < 0)
            currentEffectiveRate = 0;

        double dailyRate = currentEffectiveRate / 100.0 / 365.0;
        return (long) (this.lockedAmount * dailyRate);
    }

    /**
     * 연속 미출금 월수에 따라 패널티를 0.1%씩 복구합니다.
     */
    private void applyMonthlyRecovery() {
        if (lastWithdrawalAt == null)
            return;

        long months = ChronoUnit.MONTHS.between(lastWithdrawalAt, LocalDateTime.now());
        if (months > 0) {
            this.penaltyInterestRate -= (months * 0.1);
            if (this.penaltyInterestRate < 0) {
                this.penaltyInterestRate = 0.0;
            }
            // 복구된 개월수만큼 기준 시점을 뒤로 미룸
            this.lastWithdrawalAt = this.lastWithdrawalAt.plusMonths(months);
        }
    }

    public void matureVault() {
        if (this.status != VaultStatus.LOCKED) {
            throw new CustomException(ErrorCode.INVALID_REQUEST, "이미 해지된 금고입니다.");
        }
        this.status = VaultStatus.MATURED;
    }
}
