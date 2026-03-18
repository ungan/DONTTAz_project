package com.donttaz.donttazapi.account.domain;

import com.donttaz.donttazapi.common.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Getter
@Entity
@SQLDelete(sql = "UPDATE account SET is_deleted = true WHERE account_id = ?")
@SQLRestriction("is_deleted = false")
@Table(name = "account", uniqueConstraints = {
        @UniqueConstraint(name = "uq_account_user_bank_account", columnNames = { "user_id", "bank_code",
                "account_number" })
}, indexes = {
        @Index(name = "idx_account_user_status", columnList = "user_id, status")
})
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Account extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "account_id")
    private Long accountId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "bank_code", nullable = false, length = 10)
    private String bankCode;

    @Column(name = "bank_name", nullable = false, length = 50)
    private String bankName;

    @Column(name = "account_number", nullable = false, length = 30)
    private String accountNumber;

    @Column(name = "account_alias", length = 100)
    private String accountAlias;

    @Column(name = "account_type_unique_no", length = 50)
    private String accountTypeUniqueNo;

    @Column(length = 10)
    private String currency;

    @Column(nullable = false)
    private Long balance;

    @Column(name = "daily_transfer_limit")
    private Long dailyTransferLimit;

    @Column(name = "one_time_transfer_limit")
    private Long oneTimeTransferLimit;

    @Column(name = "linked_at", nullable = false)
    private LocalDateTime linkedAt;

    @Column(name = "last_synced_at")
    private LocalDateTime lastSyncedAt;

    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted;

    @Column(name = "is_primary", nullable = false)
    private Boolean isPrimary;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AccountStatus status;

    @Builder
    private Account(Long userId, String bankCode, String bankName, String accountNumber,
                    String accountAlias, String accountTypeUniqueNo, String currency, Long balance,
                    Long dailyTransferLimit, Long oneTimeTransferLimit,
                    LocalDateTime linkedAt, LocalDateTime lastSyncedAt, Boolean isDeleted, Boolean isPrimary, AccountStatus status) {
        this.userId = userId;
        this.bankCode = bankCode;
        this.bankName = bankName;
        this.accountNumber = accountNumber;
        this.accountAlias = accountAlias;
        this.accountTypeUniqueNo = accountTypeUniqueNo;
        this.currency = currency;
        this.balance = balance;
        this.dailyTransferLimit = dailyTransferLimit;
        this.oneTimeTransferLimit = oneTimeTransferLimit;
        this.linkedAt = linkedAt;
        this.lastSyncedAt = lastSyncedAt;
        this.isDeleted = isDeleted != null ? isDeleted : Boolean.FALSE;
        this.isPrimary = isPrimary != null ? isPrimary : Boolean.FALSE;
        this.status = status;
    }

    public void synchronize(
            String bankName,
            String accountAlias,
            String accountTypeUniqueNo,
            String currency,
            Long balance,
            Long dailyTransferLimit,
            Long oneTimeTransferLimit,
            LocalDateTime lastSyncedAt
    ) {
        this.bankName = bankName;
        if (accountAlias != null) {
            this.accountAlias = accountAlias;
        }
        if (accountTypeUniqueNo != null) {
            this.accountTypeUniqueNo = accountTypeUniqueNo;
        }
        if (currency != null) {
            this.currency = currency;
        }
        this.balance = balance;
        this.dailyTransferLimit = dailyTransferLimit;
        this.oneTimeTransferLimit = oneTimeTransferLimit;
        this.lastSyncedAt = lastSyncedAt;
        this.status = AccountStatus.ACTIVE;
    }

    public void markAsPrimary() {
        this.isPrimary = Boolean.TRUE;
    }

    public void unlink(Long balance, LocalDateTime lastSyncedAt) {
        this.balance = balance;
        this.lastSyncedAt = lastSyncedAt;
        this.isPrimary = Boolean.FALSE;
        this.status = AccountStatus.UNLINKED;
    }
}
