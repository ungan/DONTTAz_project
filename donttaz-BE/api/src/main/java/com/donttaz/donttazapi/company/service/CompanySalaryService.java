package com.donttaz.donttazapi.company.service;

import com.donttaz.donttazapi.account.domain.Account;
import com.donttaz.donttazapi.account.dto.request.AccountTransferRequest;
import com.donttaz.donttazapi.account.repository.AccountRepository;
import com.donttaz.donttazapi.account.service.AccountTransferService;
import com.donttaz.donttazapi.common.exception.CustomException;
import com.donttaz.donttazapi.common.exception.ErrorCode;
import com.donttaz.donttazapi.company.domain.Company;
import com.donttaz.donttazapi.company.domain.CompanyAccount;
import com.donttaz.donttazapi.company.dto.response.CompanyAccountResponse;
import com.donttaz.donttazapi.company.dto.response.CompanyResponse;
import com.donttaz.donttazapi.company.repository.CompanyAccountRepository;
import com.donttaz.donttazapi.company.repository.CompanyRepository;
import com.donttaz.donttazapi.user.domain.User;
import com.donttaz.donttazapi.user.repository.UserRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class CompanySalaryService {

    private final CompanyRepository companyRepository;
    private final CompanyAccountRepository companyAccountRepository;
    private final AccountRepository accountRepository;
    private final UserRepository userRepository;
    private final AccountTransferService accountTransferService;

    /**
     * 신규 기업 정보 생성함.
     */
    public Long createCompany(String name) {
        Company company = Company.builder()
                .name(name)
                .build();
        return companyRepository.save(company).getCompanyId();
    }

    /**
     * 기업에 실제 금융 계좌 연동함.
     */
    public void registerCompanyAccount(Long companyId, Long userId, String accountNumber) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new CustomException(ErrorCode.INVALID_REQUEST, "존재하지 않는 기업임."));

        CompanyAccount companyAccount = CompanyAccount.builder()
                .company(company)
                .userId(userId)
                .accountNumber(accountNumber)
                .build();
        companyAccountRepository.save(companyAccount);
    }

    /**
     * 전체 기업 목록 조회함.
     */
    @Transactional(readOnly = true)
    public List<CompanyResponse> getCompanies() {
        return companyRepository.findAll().stream()
                .map(c -> new CompanyResponse(c.getCompanyId(), c.getName()))
                .toList();
    }

    /**
     * 특정 기업의 계좌 목록 조회함.
     */
    @Transactional(readOnly = true)
    public List<CompanyAccountResponse> getCompanyAccounts(Long companyId) {
        return companyAccountRepository.findByCompany_CompanyId(companyId).stream()
                .map(ca -> new CompanyAccountResponse(ca.getCompanyAccountId(), ca.getUserId(), ca.getAccountNumber()))
                .toList();
    }

    /**
     * 월급 지급 처리함.
     * 요구사항에 맞게 입/출금 적요 자동 생성하여 이체 실행함.
     */
    public void paySalary(Long companyAccountId, String userAccountNo, Long amount) {
        CompanyAccount companyAccount = companyAccountRepository.findById(companyAccountId)
                .orElseThrow(() -> new CustomException(ErrorCode.INVALID_REQUEST, "존재하지 않는 기업 계좌임."));

        Account targetAccount = accountRepository.findByAccountNumberAndIsDeletedFalse(userAccountNo)
                .orElseThrow(() -> new CustomException(ErrorCode.ACCOUNT_NOT_FOUND));

        User targetUser = userRepository.findById(targetAccount.getUserId())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        String companyName = companyAccount.getCompany().getName();
        String userName = targetUser.getName();

        // 적요 설정: 출금(사용자명 월급 지급), 입금(회사명 월급)
        String withdrawalSummary = userName + " 월급 지급";
        String depositSummary = companyName + " 월급";

        AccountTransferRequest transferRequest = new AccountTransferRequest(
                userAccountNo,
                depositSummary,
                amount.toString(),
                companyAccount.getAccountNumber(),
                withdrawalSummary);

        // 시스템 권한으로 이체 실행함
        accountTransferService.transferBySystem(companyAccount.getUserId(), transferRequest);
    }
}
