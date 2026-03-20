package com.donttaz.donttazapi.company.domain;

import com.donttaz.donttazapi.common.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 기업의 금융 계좌 정보를 관리함.
 * 특정 기업명과 금융 API 연동을 위한 사용자 ID를 포함함.
 */
@Entity
@Table(name = "company_account")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CompanyAccount extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "company_account_id")
    private Long companyAccountId; // 기업 계좌 식별자임

    @Column(name = "user_id", nullable = false)
    private Long userId; // 금융 API용 사용자 ID임

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company; // 계좌 소유 기업임

    @Column(nullable = false, length = 30)
    private String accountNumber; // 실제 계좌 번호임

    @Builder
    public CompanyAccount(Long userId, Company company, String accountNumber) {
        this.userId = userId;
        this.company = company;
        this.accountNumber = accountNumber;
    }
}
