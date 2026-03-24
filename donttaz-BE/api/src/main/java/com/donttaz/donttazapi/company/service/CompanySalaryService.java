package com.donttaz.donttazapi.company.service;

import com.donttaz.donttazapi.account.domain.Account;
import com.donttaz.donttazapi.account.domain.AccountStatus;
import com.donttaz.donttazapi.account.dto.request.AccountDepositRequest;
import com.donttaz.donttazapi.account.dto.request.AccountTransferRequest;
import com.donttaz.donttazapi.account.repository.AccountRepository;
import com.donttaz.donttazapi.account.service.AccountTransferService;
import com.donttaz.donttazapi.common.exception.CustomException;
import com.donttaz.donttazapi.common.exception.ErrorCode;
import com.donttaz.donttazapi.company.domain.Company;
import com.donttaz.donttazapi.company.domain.CompanyAccount;
import com.donttaz.donttazapi.company.dto.response.CompanyAccountResponse;
import com.donttaz.donttazapi.company.dto.response.CompanyResponse;
import com.donttaz.donttazapi.company.dto.response.UserAccountSummaryResponse;
import com.donttaz.donttazapi.company.repository.CompanyAccountRepository;
import com.donttaz.donttazapi.company.repository.CompanyRepository;
import com.donttaz.donttazapi.infra.banking.client.BankingClient;
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

        private static final String COMPANY_SALARY_ACCOUNT_TYPE = "003-1-befca2dba9314d"; // 월급지급 전용 맞춤 계좌 상품 번호

        private final CompanyRepository companyRepository;
        private final CompanyAccountRepository companyAccountRepository;
        private final AccountRepository accountRepository;
        private final UserRepository userRepository;
        private final AccountTransferService accountTransferService;
        private final BankingClient bankingClient; // 금융 API 클라이언트 추가함

        /**
         * 기업 정보 생성 및 계좌 자동 연동 (1억 입금 포함)
         */
        public Long createCompany(String name, Long userId) {
                Company company = Company.builder()
                                .name(name)
                                .build();
                Long createdCompanyId = companyRepository.save(company).getCompanyId(); // 생성 기업 ID 반환

                // 2단계: 기업 계좌 자동 개설
                createCompanyAccount(createdCompanyId, userId);

                // 3단계: 개설된 계좌에 즉시 테스트 자금 1억 강제 충전
                deposit100Million(createdCompanyId);

                return createdCompanyId;
        }

        /**
         * 기업 계좌 직접 생성
         */
        public void createCompanyAccount(Long companyId, Long userId) {
                Company company = companyRepository.findById(companyId)
                                .orElseThrow(() -> new CustomException(ErrorCode.INVALID_REQUEST, "존재하지 않는 기업임."));

                // 금융 API 통해 사용자가 지정한 월급 전용 계좌 상품으로 개설
                String accountNumber = bankingClient.createDemandDepositAccount(userId, COMPANY_SALARY_ACCOUNT_TYPE)
                                .accountNo();

                CompanyAccount companyAccount = CompanyAccount.builder()
                                .company(company)
                                .userId(userId)
                                .accountNumber(accountNumber)
                                .build();
                companyAccountRepository.save(companyAccount); // 생성 계좌 정보 저장
        }

        /**
         * 전체 기업 목록 조회
         */
        @Transactional(readOnly = true)
        public List<CompanyResponse> getCompanies() {
                return companyRepository.findAll().stream()
                                .map(c -> new CompanyResponse(c.getCompanyId(), c.getName()))
                                .toList();
        }

        /**
         * 특정 기업 계좌 목록 조회
         */
        @Transactional(readOnly = true)
        public List<CompanyAccountResponse> getCompanyAccounts(Long companyId) {
                return companyAccountRepository.findByCompany_CompanyId(companyId).stream()
                                .map(ca -> new CompanyAccountResponse(ca.getCompanyAccountId(), ca.getUserId(),
                                                ca.getAccountNumber()))
                                .toList();
        }

        /**
         * 월급 지급 처리
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

                // 시스템 이체 실행
                accountTransferService.transfer(companyAccount.getUserId(), transferRequest);
        }

        /**
         * 전체 사용자 계좌 번호 직관적 조회 (Swagger 보조용)
         */
        @Transactional(readOnly = true)
        public List<UserAccountSummaryResponse> getAllUserAccounts() {
                return accountRepository.findAll().stream()
                                .filter(account -> !account.getIsDeleted() && account.getStatus() == AccountStatus.ACTIVE)
                                .map(account -> {
                                        User user = userRepository.findById(account.getUserId()).orElse(null);
                                        String userName = (user != null) ? user.getName() : "알 수 없음";
                                        return new UserAccountSummaryResponse(account.getUserId(), userName, account.getAccountNumber());
                                })
                                .toList();
        }

        /**
         * 기업 계좌 1억 원 강제 입금 (테스트/Swagger용)
         */
        public void deposit100Million(Long companyId) {
                CompanyAccount companyAccount = companyAccountRepository.findByCompany_CompanyId(companyId).stream().findFirst()
                                .orElseThrow(() -> new CustomException(ErrorCode.INVALID_REQUEST, "기업 계좌가 하나도 존재하지 않음. 계좌를 먼저 생성해 주세요."));

                AccountDepositRequest request = new AccountDepositRequest("100000000", "테스트 1억 원 강제 입금");
                accountTransferService.deposit(companyAccount.getUserId(), companyAccount.getAccountNumber(), request);
        }
}
