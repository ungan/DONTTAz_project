package com.donttaz.donttazapi.account.domain;

import com.donttaz.donttazapi.common.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "accounts")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Account extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false, length = 10)
    private String bankCode;

    @Column(nullable = false, length = 50)
    private String bankName;

    @Column(nullable = false, length = 30)
    private String accountNumber;

    @Column(length = 100)
    private String accountAlias;

    @Column(nullable = false)
    private Integer balance;

    @Column(nullable = false)
    private LocalDateTime linkedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AccountStatus status;

    @Builder
    private Account(Long userId, String bankCode, String bankName, String accountNumber,
                    String accountAlias, Integer balance, LocalDateTime linkedAt, AccountStatus status) {
        this.userId = userId;
        this.bankCode = bankCode;
        this.bankName = bankName;
        this.accountNumber = accountNumber;
        this.accountAlias = accountAlias;
        this.balance = balance;
        this.linkedAt = linkedAt;
        this.status = status;
    }
}
