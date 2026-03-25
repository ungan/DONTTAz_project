package com.donttaz.donttazapi.company.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.donttaz.donttazapi.account.domain.Account;
import com.donttaz.donttazapi.account.dto.request.AccountTransferRequest;
import com.donttaz.donttazapi.account.repository.AccountRepository;
import com.donttaz.donttazapi.account.service.AccountTransferService;
import com.donttaz.donttazapi.account.service.dto.DemandDepositAccountCreateResult;
import com.donttaz.donttazapi.company.domain.Company;
import com.donttaz.donttazapi.company.domain.CompanyAccount;
import com.donttaz.donttazapi.company.repository.CompanyAccountRepository;
import com.donttaz.donttazapi.company.repository.CompanyRepository;
import com.donttaz.donttazapi.infra.banking.client.BankingClient;
import com.donttaz.donttazapi.notification.service.PushNotificationService;
import com.donttaz.donttazapi.user.domain.User;
import com.donttaz.donttazapi.user.repository.UserRepository;
import com.donttaz.donttazapi.vault.service.SalaryAutoTransferService;
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
    private BankingClient bankingClient;

    @Mock
    private PushNotificationService pushNotificationService;

    @Mock
    private SalaryAutoTransferService salaryAutoTransferService;

    @InjectMocks
    private CompanySalaryService companySalaryService;

    @Test
    @DisplayName("기업 계좌 신규 생성 및 저장이 정상적으로 수행되어야 한다")
    void createCompanyAccountShouldCreateAndSaveAccount() {
        // given
        Long companyId = 1L;
        Long userId = 100L;
        String generatedAccountNo = "1234567890";
        Company company = Company.builder().name("테스트기업").build();

        given(companyRepository.findById(companyId)).willReturn(Optional.of(company));
        given(bankingClient.createDemandDepositAccount(eq(userId), any())).willReturn(
                new DemandDepositAccountCreateResult("001", generatedAccountNo, "KRW", "원"));

        // when
        companySalaryService.createCompanyAccount(companyId, userId);

        // then
        // 금융 API를 통해 월급 전용 계좌 상품으로 계좌를 개설해야 한다.
        verify(bankingClient).createDemandDepositAccount(eq(userId), eq("003-1-befca2dba9314d"));
        // 생성된 기업 계좌 정보는 저장소에 저장되어야 한다.
        verify(companyAccountRepository).save(any(CompanyAccount.class));
    }

    @Test
    @DisplayName("급여 지급 시 이체 적요와 알림, 자동 이체가 정상 처리되어야 한다")
    void paySalaryShouldTransferNotifyAndTriggerAutoTransfer() {
        // given
        Long companyAccountId = 10L;
        Long adminUserId = 100L;
        String companyName = "테스트기업";
        Company company = Company.builder().name(companyName).build();

        String companyAccountNo = "1111111111";
        CompanyAccount companyAccount = CompanyAccount.builder()
                .company(company)
                .userId(adminUserId)
                .accountNumber(companyAccountNo)
                .build();

        String userAccountNo = "2222222222";
        Long targetUserId = 200L;
        String userName = "홍길동";
        Account targetAccount = Account.builder()
                .userId(targetUserId)
                .accountNumber(userAccountNo)
                .balance(0L)
                .build();
        User targetUser = User.builder().name(userName).build();

        given(companyAccountRepository.findById(companyAccountId)).willReturn(Optional.of(companyAccount));
        given(accountRepository.findByAccountNumberAndIsDeletedFalse(userAccountNo))
                .willReturn(Optional.of(targetAccount));
        given(userRepository.findById(targetUserId)).willReturn(Optional.of(targetUser));

        Long amount = 3000000L;

        // when
        companySalaryService.paySalary(companyAccountId, userAccountNo, amount);

        // then
        String expectedWithdrawalSummary = userName + " 월급 지급";
        String expectedDepositSummary = companyName + " 월급";

        // 이체 요청에 출금/입금 적요와 계좌 정보가 올바르게 들어가야 한다.
        verify(accountTransferService).transfer(
                eq(adminUserId),
                argThat((AccountTransferRequest request) ->
                        request.withdrawalTransactionSummary().equals(expectedWithdrawalSummary)
                                && request.depositTransactionSummary().equals(expectedDepositSummary)
                                && request.transactionBalance().equals(amount.toString())
                                && request.withdrawalAccountNo().equals(companyAccountNo)
                                && request.depositAccountNo().equals(userAccountNo))
        );

        // 급여 입금 알림이 대상 사용자에게 전달되어야 한다.
        verify(pushNotificationService).sendDepositNotification(targetUserId, amount, expectedDepositSummary);

        // 급여 지급 직후 금고 자동 이체 로직도 연쇄 실행되어야 한다.
        verify(salaryAutoTransferService).processUserSalaryTransfer(eq(targetUser));
    }

    @Test
    @DisplayName("급여 수령자와 기업 계좌 소유자가 같으면 입금 알림을 보내지 않아야 한다")
    void paySalaryShouldSkipDuplicateDepositNotificationForSameUser() {
        // given
        Long companyAccountId = 10L;
        Long sameUserId = 100L;
        Company company = Company.builder().name("테스트기업").build();
        CompanyAccount companyAccount = CompanyAccount.builder()
                .company(company)
                .userId(sameUserId)
                .accountNumber("1111111111")
                .build();
        Account targetAccount = Account.builder()
                .userId(sameUserId)
                .accountNumber("2222222222")
                .balance(0L)
                .build();
        User targetUser = User.builder().name("홍길동").build();

        given(companyAccountRepository.findById(companyAccountId)).willReturn(Optional.of(companyAccount));
        given(accountRepository.findByAccountNumberAndIsDeletedFalse("2222222222"))
                .willReturn(Optional.of(targetAccount));
        given(userRepository.findById(sameUserId)).willReturn(Optional.of(targetUser));

        // when
        companySalaryService.paySalary(companyAccountId, "2222222222", 3000000L);

        // then
        // 회사 계좌 소유자와 급여 수령자가 같으면 중복 입금 알림은 보내지 않는다.
        verify(pushNotificationService, never()).sendDepositNotification(any(), any(), any());
        // 알림 생략과 무관하게 금고 자동 이체는 계속 수행되어야 한다.
        verify(salaryAutoTransferService).processUserSalaryTransfer(eq(targetUser));
    }
}
