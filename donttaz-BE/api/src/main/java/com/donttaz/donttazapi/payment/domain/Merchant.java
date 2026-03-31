package com.donttaz.donttazapi.payment.domain;

import com.donttaz.donttazapi.common.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(
        name = "merchant",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_merchant_settlement_account", columnNames = "settlement_account_no")
        },
        indexes = {
                @Index(name = "idx_merchant_active_name", columnList = "active, name")
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Merchant extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "merchant_id")
    private Long merchantId;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "settlement_account_no", nullable = false, length = 30)
    private String settlementAccountNo;

    @Column(length = 50)
    private String category;

    @Column(nullable = false)
    private Boolean active;

    @Builder
    private Merchant(String name, String settlementAccountNo, String category, Boolean active) {
        this.name = name;
        this.settlementAccountNo = settlementAccountNo;
        this.category = category;
        this.active = active != null ? active : Boolean.TRUE;
    }
}
