package com.donttaz.donttazapi.company.domain;

import com.donttaz.donttazapi.common.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 기업 정보를 관리하는 엔티티임.
 * 사업자 정보 대신 기업 명칭만 관리함.
 */
@Entity
@Table(name = "company")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Company extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "company_id")
    private Long companyId; // 기업 식별자임

    @Column(nullable = false, length = 100)
    private String name; // 기업 명칭임

    @Builder
    public Company(String name) {
        this.name = name; // 기업 명 생성함
    }
}
