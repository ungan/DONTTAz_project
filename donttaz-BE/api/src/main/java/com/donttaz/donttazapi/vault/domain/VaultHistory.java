package com.donttaz.donttazapi.vault.domain;

import com.donttaz.donttazapi.common.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "vault_history")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class VaultHistory extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "vault_history_id")
    private Long id;

    @Column(name = "vault_id", nullable = false)
    private Long vaultId;

    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", nullable = false, length = 20)
    private TransactionType transactionType;

    @Column(nullable = false)
    private Long amount;

    @Column(name = "interest_paid", nullable = false)
    private Long interestPaid;

    @Builder
    private VaultHistory(Long vaultId, TransactionType transactionType, Long amount, Long interestPaid) {
        this.vaultId = vaultId;
        this.transactionType = transactionType;
        this.amount = amount;
        this.interestPaid = interestPaid != null ? interestPaid : 0L;
    }
}
