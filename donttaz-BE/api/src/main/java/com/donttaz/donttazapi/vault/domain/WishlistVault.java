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
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "wishlist_vault", indexes = {
        @Index(name = "idx_wishlist_vault_user_status", columnList = "user_id, status")
})
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class WishlistVault extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "wishlist_vault_id")
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "wish_name", length = 100)
    private String wishName;

    @Column(name = "goal_amount")
    private Long goalAmount;

    @Column(name = "locked_amount", nullable = false)
    private Long lockedAmount;

    @Column(name = "base_interest_rate", nullable = false)
    private Double baseInterestRate;

    @Column(name = "penalty_interest_rate", nullable = false)
    private Double penaltyInterestRate;

    @Column(name = "withdrawal_count", nullable = false)
    private Integer withdrawalCount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private VaultStatus status;

    @Column(name = "last_withdrawal_at")
    private LocalDateTime lastWithdrawalAt;

    @Builder
    private WishlistVault(Long userId, String wishName, Long goalAmount, Long lockedAmount, Double penaltyInterestRate,
            VaultStatus status) {
        this.userId = userId;
        this.wishName = wishName;
        this.goalAmount = goalAmount;
        this.lockedAmount = lockedAmount != null ? lockedAmount : 0L;
        this.baseInterestRate = 6.0; // 6% 고정
        this.penaltyInterestRate = penaltyInterestRate != null ? penaltyInterestRate : 0.0;
        this.withdrawalCount = 0;
        this.status = status != null ? status : VaultStatus.LOCKED;
        this.lastWithdrawalAt = LocalDateTime.now();
    }

    public void deposit(Long amount) {
        if (amount == null || amount <= 0) {
            throw new CustomException(ErrorCode.INVALID_REQUEST, "입금액은 0보다 커야 합니다.");
        }
        this.lockedAmount += amount;
    }

    public void addInterest(Long interestAmount) {
        if (interestAmount != null && interestAmount > 0) {
            this.lockedAmount += interestAmount;
        }
    }

    public void withdraw(Long amount) {
        if (this.status != VaultStatus.LOCKED) {
            throw new CustomException(ErrorCode.INVALID_REQUEST, "잠금 상태인 금고만 출금할 수 있습니다.");
        }
        if (amount == null || amount <= 0 || amount > this.lockedAmount) {
            throw new CustomException(ErrorCode.INVALID_REQUEST, "출금액이 올바르지 않거나 잔액이 부족합니다.");
        }

        if (this.withdrawalCount >= 3) {
            throw new CustomException(ErrorCode.INVALID_REQUEST,
                    "위시리스트 금고는 최대 3회까지만 부분 출금이 가능합니다. 더 이상 출금이 필요하시면 금고를 해지해주세요.");
        }

        applyMonthlyRecovery();

        this.lockedAmount -= amount;
        this.withdrawalCount++;

        // 위시리스트 패널티 0.3%p
        this.penaltyInterestRate += 0.3;
        this.lastWithdrawalAt = LocalDateTime.now();

        if (this.lockedAmount == 0) {
            this.status = VaultStatus.BROKEN;
        }
    }

    public long matureVault() {
        if (this.status != VaultStatus.LOCKED) {
            throw new CustomException(ErrorCode.INVALID_REQUEST, "이미 해지된 금고입니다.");
        }

        if (this.withdrawalCount == 0) {
            Long bonus = (long) (this.lockedAmount * 0.01);
            this.lockedAmount += bonus;
        }

        long finalBalance = this.lockedAmount;
        this.lockedAmount = 0L;
        this.status = VaultStatus.MATURED;
        return finalBalance;
    }

    public long calculateInterest() {
        if (this.status != VaultStatus.LOCKED)
            return 0L;

        applyMonthlyRecovery();

        double currentEffectiveRate = this.baseInterestRate - this.penaltyInterestRate;
        if (currentEffectiveRate < 0)
            currentEffectiveRate = 0;

        long days = ChronoUnit.DAYS.between(this.getCreatedAt(), LocalDateTime.now());
        if (days <= 0)
            return 0L;

        return (long) (this.lockedAmount * (currentEffectiveRate / 100.0) * (days / 365.0));
    }

    public long calculateDailyInterest() {
        if (this.status != VaultStatus.LOCKED)
            return 0L;

        applyMonthlyRecovery();

        double currentEffectiveRate = this.baseInterestRate - this.penaltyInterestRate;
        if (currentEffectiveRate < 0)
            currentEffectiveRate = 0;

        return (long) (this.lockedAmount * (currentEffectiveRate / 100.0) / 365.0);
    }

    public void applyMonthlyRecovery() {
        if (this.lastWithdrawalAt == null)
            return;

        long months = ChronoUnit.MONTHS.between(this.lastWithdrawalAt, LocalDateTime.now());
        if (months > 0) {
            this.penaltyInterestRate -= (months * 0.1);
            if (this.penaltyInterestRate < 0.0) {
                this.penaltyInterestRate = 0.0;
            }
            this.lastWithdrawalAt = this.lastWithdrawalAt.plusMonths(months);
        }
    }
}
