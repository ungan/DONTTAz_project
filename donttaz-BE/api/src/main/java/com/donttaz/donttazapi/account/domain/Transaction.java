package com.donttaz.donttazapi.account.domain;

import com.donttaz.donttazapi.common.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(
        name = "account_transaction",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_account_transaction_account_source_tx",
                        columnNames = {"account_id", "source", "source_tx_id"}
                )
        },
        indexes = {
                @Index(name = "idx_account_transaction_account_transaction_at", columnList = "account_id, transaction_at"),
                @Index(name = "idx_account_transaction_user_transaction_at", columnList = "user_id, transaction_at"),
                @Index(name = "idx_account_transaction_account_type_transaction_at", columnList = "account_id, tx_type, transaction_at")
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Transaction extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "account_tx_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "source", nullable = false, length = 20)
    private String source;

    @Column(name = "source_tx_id", nullable = false, length = 64)
    private String sourceTxId;

    @Column(name = "tx_type", nullable = false, length = 20)
    private String txType;

    @Column(name = "tx_type_name", length = 20)
    private String txTypeName;

    @Column(nullable = false)
    private Long amount;

    @Column(name = "balance_snapshot", nullable = false)
    private Long balanceSnapshot;

    @Column(name = "counterparty_name", length = 100)
    private String counterpartyName;

    @Column(name = "ai_category", length = 50)
    private String aiCategory;

    @Column(name = "counterparty_bank", length = 100)
    private String counterpartyBank;

    @Column(name = "counterparty_account_no", length = 30)
    private String counterpartyAccountNo;

    @Column(name = "transaction_summary", length = 255)
    private String transactionSummary;

    @Column(name = "transaction_at", nullable = false)
    private LocalDateTime transactionAt;

    @Column(name = "synced_at", nullable = false)
    private LocalDateTime syncedAt;

    @Builder
    private Transaction(
            Account account,
            Long userId,
            String source,
            String sourceTxId,
            String txType,
            String txTypeName,
            Long amount,
            Long balanceSnapshot,
            String counterpartyName,
            String aiCategory,
            String counterpartyBank,
            String counterpartyAccountNo,
            String transactionSummary,
            LocalDateTime transactionAt,
            LocalDateTime syncedAt
    ) {
        this.account = account;
        this.userId = userId;
        this.source = source;
        this.sourceTxId = sourceTxId;
        this.txType = txType;
        this.txTypeName = txTypeName;
        this.amount = amount;
        this.balanceSnapshot = balanceSnapshot;
        this.counterpartyName = counterpartyName;
        this.aiCategory = aiCategory;
        this.counterpartyBank = counterpartyBank;
        this.counterpartyAccountNo = counterpartyAccountNo;
        this.transactionSummary = transactionSummary;
        this.transactionAt = transactionAt;
        this.syncedAt = syncedAt;
    }
}
