package com.donttaz.donttazapi.company.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.donttaz.donttazapi.account.domain.Account;
import com.donttaz.donttazapi.account.dto.request.AccountTransferRequest;
import com.donttaz.donttazapi.account.repository.AccountRepository;
import com.donttaz.donttazapi.account.service.AccountTransferService;
import com.donttaz.donttazapi.company.domain.Company;
import com.donttaz.donttazapi.company.domain.CompanyAccount;
import com.donttaz.donttazapi.company.repository.CompanyAccountRepository;
import com.donttaz.donttazapi.company.repository.CompanyRepository;
import com.donttaz.donttazapi.infra.banking.client.BankingClient;
import com.donttaz.donttazapi.account.service.dto.DemandDepositAccountCreateResult;
import com.donttaz.donttazapi.vault.service.SalaryAutoTransferService;
import com.donttaz.donttazapi.user.domain.User;
import com.donttaz.donttazapi.user.repository.UserRepository;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CompanySalaryServiceTest {

        @Mock
        private CompanyRepository companyRepository;
        @Mock
        private CompanyAccountRepository companyAccountRepository;
        @Mock
        private AccountRepository accountRepository;
        @Mock
        private UserRepository userRepository;
        @Mock
        private AccountTransferService accountTransferService;
        @Mock
        private BankingClient bankingClient; // 금융 API 클라이언트 모킹 추가함
        @Mock
        private SalaryAutoTransferService salaryAutoTransferService;

        @InjectMocks
        private CompanySalaryService companySalaryService;

        @Test
        @DisplayName("기업 계좌 신규 생성 및 저장이 정상적으로 수행되어야 한다")
        void createCompanyAccount_ShouldCreateAndSaveAccount() {
                // given
                Long companyId = 1L;
                Long userId = 100L;
                String generatedAccountNo = "1234567890";
                Company company = Company.builder().name("테스트기업").build();

                given(companyRepository.findById(companyId)).willReturn(Optional.of(company));
                given(bankingClient.createDemandDepositAccount(eq(userId), any())).willReturn(
                                new DemandDepositAccountCreateResult("001", generatedAccountNo, "KRW", "원"));

                // when
                companySalaryService.createCompanyAccount(companyId, userId); // 계좌 생성 실행함

                // then
                // 금융 API 호출 확인
                verify(bankingClient).createDemandDepositAccount(eq(userId), eq("003-1-befca2dba9314d"));
                // DB 저장 확인
                verify(companyAccountRepository).save(any(CompanyAccount.class));
        }

        @Test
        @DisplayName("급여 지급 시 출금 및 입금 적요가 올바르게 설정되어야 한다")
        void paySalary_ShouldSetCorrectSummaries() {
                // given
                Long companyAccountId = 10L;
                Long adminUserId = 100L;
                String companyName = "테스트기업";
                Company company = Company.builder().name(companyName).build(); // 기업 객체 생성

                String companyAccountNo = "1111111111";
                CompanyAccount companyAccount = CompanyAccount.builder()
                                .company(company)
                                .userId(adminUserId)
                                .accountNumber(companyAccountNo)
                                .build(); // 기업 계좌 정보 구성

                String userAccountNo = "2222222222";
                Long targetUserId = 200L;
                String userName = "홍길동";
                Account targetAccount = Account.builder()
                                .userId(targetUserId)
                                .accountNumber(userAccountNo)
                                .balance(0L)
                                .build(); // 대상 사용자 계좌 생성
                User targetUser = User.builder().name(userName).build(); // 대상 사용자 정보 생성

                given(companyAccountRepository.findById(companyAccountId)).willReturn(Optional.of(companyAccount));
                given(accountRepository.findByAccountNumberAndIsDeletedFalse(userAccountNo))
                                .willReturn(Optional.of(targetAccount));
                given(userRepository.findById(targetUserId)).willReturn(Optional.of(targetUser));

                Long amount = 3000000L;

                // when
                companySalaryService.paySalary(companyAccountId, userAccountNo, amount); // 급여 지급 실행

                // then
                String expectedWithdrawalSummary = userName + " 월급 지급";
                String expectedDepositSummary = companyName + " 월급";

                // 시스템 이체 메서드 호출 여부 검토
                verify(accountTransferService).transfer(eq(adminUserId), any(AccountTransferRequest.class));

                // 전달된 요청 데이터의 정확성 검증
        verify(accountTransferService).transfer(eq(adminUserId),
                        argThat((AccountTransferRequest request) -> request.withdrawalTransactionSummary()
                                        .equals(expectedWithdrawalSummary) &&
                                        request.depositTransactionSummary().equals(expectedDepositSummary) &&
                                        request.transactionBalance().equals(amount.toString()) &&
                                        request.withdrawalAccountNo().equals(companyAccountNo) &&
                                        request.depositAccountNo().equals(userAccountNo)));

                // 금고 연쇄 이체 트리거가 호출되었는지 확인
                verify(salaryAutoTransferService).processUserSalaryTransfer(eq(targetUser));
        }

        /*
         * ArgumentMatchers 헬퍼 메서드임.
         *
         * private <T> T argThat(java.util.function.Predicate<T> predicate) {
         *         return org.mockito.ArgumentMatchers.argThat(argument -> predicate.test(argument));
         * }
         */
}
