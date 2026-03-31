package com.donttaz.donttazapi.company.repository;

import com.donttaz.donttazapi.company.domain.CompanyAccount;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CompanyAccountRepository extends JpaRepository<CompanyAccount, Long> {
    List<CompanyAccount> findByCompany_CompanyId(Long companyId); // 특정 기업의 계좌 목록 조회함
}
