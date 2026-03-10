package com.donttaz.donttazapi.account.domain;

import com.donttaz.donttazapi.common.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "transactions")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Transaction extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TransactionType type;

    @Column(nullable = false)
    private Integer amount;

    @Column(nullable = false)
    private Integer balanceAfter;

    @Column(nullable = false, length = 100)
    private String counterpartyName;

    @Column(length = 50)
    private String counterpartyBank;

    @Column(length = 30)
    private String counterpartyAccount;

    @Column(nullable = false)
    private Integer fee;

    @Column(length = 100)
    private String category;

    @Column(length = 255)
    private String memo;

    @Column(nullable = false)
    private LocalDateTime transactionDate;

    @Builder
    private Transaction(Account account, TransactionType type, Integer amount, Integer balanceAfter,
                        String counterpartyName, String counterpartyBank, String counterpartyAccount,
                        Integer fee, String category, String memo, LocalDateTime transactionDate) {
        this.account = account;
        this.type = type;
        this.amount = amount;
        this.balanceAfter = balanceAfter;
        this.counterpartyName = counterpartyName;
        this.counterpartyBank = counterpartyBank;
        this.counterpartyAccount = counterpartyAccount;
        this.fee = fee;
        this.category = category;
        this.memo = memo;
        this.transactionDate = transactionDate;
    }
}
