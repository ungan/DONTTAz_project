package com.donttaz.donttazapi.payment.domain;

import com.donttaz.donttazapi.common.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
        name = "payment",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_payment_user_withdrawal_tx", columnNames = {"user_id", "withdrawal_transaction_id"})
        },
        indexes = {
                @Index(name = "idx_payment_user_withdrawal_tx", columnList = "user_id, withdrawal_transaction_id"),
                @Index(name = "idx_payment_merchant_paid_at", columnList = "merchant_id, paid_at")
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Payment extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "payment_id")
    private Long paymentId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "merchant_id", nullable = false)
    private Merchant merchant;

    @Column(name = "merchant_name", nullable = false, length = 100)
    private String merchantName;

    @Column(name = "withdrawal_account_no", nullable = false, length = 30)
    private String withdrawalAccountNo;

    @Column(name = "settlement_account_no", nullable = false, length = 30)
    private String settlementAccountNo;

    @Column(name = "withdrawal_transaction_id", nullable = false, length = 64)
    private String withdrawalTransactionId;

    @Column(name = "deposit_transaction_id", length = 64)
    private String depositTransactionId;

    @Column(nullable = false)
    private Long amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PaymentStatus status;

    @Column(name = "paid_at", nullable = false)
    private LocalDateTime paidAt;

    @Builder
    private Payment(
            Long userId,
            Merchant merchant,
            String merchantName,
            String withdrawalAccountNo,
            String settlementAccountNo,
            String withdrawalTransactionId,
            String depositTransactionId,
            Long amount,
            PaymentStatus status,
            LocalDateTime paidAt
    ) {
        this.userId = userId;
        this.merchant = merchant;
        this.merchantName = merchantName;
        this.withdrawalAccountNo = withdrawalAccountNo;
        this.settlementAccountNo = settlementAccountNo;
        this.withdrawalTransactionId = withdrawalTransactionId;
        this.depositTransactionId = depositTransactionId;
        this.amount = amount;
        this.status = status;
        this.paidAt = paidAt;
    }
}
