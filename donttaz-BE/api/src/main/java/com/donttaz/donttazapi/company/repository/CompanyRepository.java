package com.donttaz.donttazapi.company.repository;

import com.donttaz.donttazapi.company.domain.Company;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 기업 엔티티 관리용 리포지토리임.
 */
public interface CompanyRepository extends JpaRepository<Company, Long> {
}
