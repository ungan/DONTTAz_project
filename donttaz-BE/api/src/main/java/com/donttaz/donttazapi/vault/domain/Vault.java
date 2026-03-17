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
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "vault", indexes = {
        @Index(name = "idx_vault_user_kind", columnList = "user_id, vault_kind"),
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

    @Column(nullable = false)
    private Long balance;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private VaultStatus status;

    // ===== WISHLIST 전용 컬럼 (vaultKind = WISHLIST 일 때만 사용, nullable) =====
    @Column(name = "wishlist_name", length = 100)
    private String wishlistName;

    @Column(name = "goal_amount")
    private Long goalAmount;

    @Column(name = "goal_date")
    private LocalDate goalDate;

    @Column(name = "is_achieved")
    private Boolean isAchieved;

    @Builder
    private Vault(Long userId, VaultKind vaultKind, Long balance, VaultStatus status,
            String wishlistName, Long goalAmount, LocalDate goalDate, Boolean isAchieved) {
        this.userId = userId;
        this.vaultKind = vaultKind;
        this.balance = balance != null ? balance : 0L;
        this.status = status != null ? status : VaultStatus.LOCKED;
        this.wishlistName = wishlistName;
        this.goalAmount = goalAmount;
        this.goalDate = goalDate;
        this.isAchieved = isAchieved != null ? isAchieved : Boolean.FALSE;
    }

    public void deposit(Long amount) {
        if (amount == null || amount <= 0) {
            throw new CustomException(ErrorCode.INVALID_REQUEST, "입금액은 0보다 커야 합니다.");
        }
        this.balance += amount;
    }

    public void withdraw(Long amount) {
        if (amount == null || amount <= 0) {
            throw new CustomException(ErrorCode.INVALID_REQUEST, "출금액은 0보다 커야 합니다.");
        }
        if (this.balance < amount) {
            throw new CustomException(ErrorCode.VAULT_INSUFFICIENT_BALANCE);
        }
        this.balance -= amount;
    }

    public void setWishlistGoal(String wishlistName, Long goalAmount, LocalDate goalDate) {
        if (this.vaultKind != VaultKind.WISHLIST) {
            throw new CustomException(ErrorCode.INVALID_REQUEST, "위시리스트 금고에만 목표를 설정할 수 있습니다.");
        }
        this.wishlistName = wishlistName;
        this.goalAmount = goalAmount;
        this.goalDate = goalDate;
    }

    public void achieve() {
        if (this.vaultKind != VaultKind.WISHLIST) {
            throw new CustomException(ErrorCode.INVALID_REQUEST, "위시리스트 금고만 달성할 수 있습니다.");
        }
        if (Boolean.TRUE.equals(this.isAchieved) || this.status == VaultStatus.CLOSED) {
            throw new CustomException(ErrorCode.VAULT_ALREADY_ACHIEVED);
        }
        if (this.goalAmount == null || this.balance < this.goalAmount) {
            throw new CustomException(ErrorCode.VAULT_GOAL_NOT_MET);
        }
        this.isAchieved = Boolean.TRUE;
        this.status = VaultStatus.CLOSED;
    }
}
