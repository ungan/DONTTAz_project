package com.donttaz.donttazapi.account.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.donttaz.donttazapi.account.domain.Account;
import com.donttaz.donttazapi.account.domain.AccountStatus;
import com.donttaz.donttazapi.account.repository.AccountRepository;
import com.donttaz.donttazapi.account.service.dto.AccountDetailResult;
import com.donttaz.donttazapi.account.service.dto.DemandDepositAccountResult;
import com.donttaz.donttazapi.common.security.CurrentUserProvider;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AccountSyncServiceTest {

        @Mock
        private AccountRepository accountRepository;

        @Mock
        private BankingAccountQueryService bankingAccountQueryService;

        @Mock
        private CurrentUserProvider currentUserProvider;

        @InjectMocks
        private AccountSyncService accountSyncService;

        @Test
        @DisplayName("외부 계좌 목록 동기화 시 누락된 내부 계좌를 생성한다.")
        void syncDemandDepositAccountsCreatesNewAccount() {
                DemandDepositAccountResult externalAccount = new DemandDepositAccountResult(
                                "001",
                                "BANK",
                                "USER",
                                "12345678901234",
                                "ACCOUNT",
                                "1",
                                "TYPE",
                                "20240101",
                                "20290101",
                                "1000000",
                                "100000",
                                "7000",
                                "20240102",
                                "KRW");
                given(currentUserProvider.getCurrentUserId()).willReturn(1L);
                given(accountRepository.findByUserIdAndBankCodeAndAccountNumber(1L, "001", "12345678901234"))
                                .willReturn(Optional.empty());

                accountSyncService.syncDemandDepositAccounts(List.of(externalAccount));

        ArgumentCaptor<Account> captor = ArgumentCaptor.forClass(Account.class);
        verify(accountRepository).save(captor.capture());
        Account saved = captor.getValue();
        assertThat(saved.getUserId()).isEqualTo(1L);
        assertThat(saved.getBankCode()).isEqualTo("001");
        assertThat(saved.getBankName()).isEqualTo("BANK");
        assertThat(saved.getAccountNumber()).isEqualTo("12345678901234");
        assertThat(saved.getCurrency()).isEqualTo("KRW");
        assertThat(saved.getBalance()).isEqualTo(7000L);
        assertThat(saved.getDailyTransferLimit()).isEqualTo(1_000_000L);
        assertThat(saved.getOneTimeTransferLimit()).isEqualTo(100_000L);
        assertThat(saved.getStatus()).isEqualTo(AccountStatus.ACTIVE);
        assertThat(saved.getIsDeleted()).isFalse();
        assertThat(saved.getLinkedAt()).isNotNull();
        assertThat(saved.getLastSyncedAt()).isNotNull();
    }

        @Test
        @DisplayName("생성된 계좌 동기화 시 기존 계좌를 복구하고 최신 값으로 갱신한다.")
        void syncCreatedAccountUpdatesExistingAccount() {
                Account account = Account.builder()
                                .userId(1L)
                                .bankCode("001")
                                .bankName("OLD")
                                .accountNumber("12345678901234")
                                .balance(0L)
                                .linkedAt(LocalDateTime.of(2024, 1, 1, 0, 0))
                                .lastSyncedAt(LocalDateTime.of(2024, 1, 1, 0, 0))
                                .status(AccountStatus.UNLINKED)
                                .build();
                AccountDetailResult detail = new AccountDetailResult(
                                "001",
                                "NEW BANK",
                                "USER",
                                "12345678901234",
                                "ACCOUNT",
                                "1",
                                "TYPE",
                                "20240101",
                                "20290101",
                                "1000000",
                                "100000",
                                "9000",
                                "20240102",
                                "KRW");
                given(currentUserProvider.getCurrentUserId()).willReturn(1L);
                given(bankingAccountQueryService.getAccountDetail("12345678901234")).willReturn(detail);
                given(accountRepository.findByUserIdAndBankCodeAndAccountNumber(1L, "001", "12345678901234"))
                                .willReturn(Optional.of(account));

                accountSyncService.syncCreatedAccount("001-1-abc", "12345678901234");

        assertThat(account.getBankName()).isEqualTo("NEW BANK");
        assertThat(account.getAccountTypeUniqueNo()).isEqualTo("001-1-abc");
        assertThat(account.getCurrency()).isEqualTo("KRW");
        assertThat(account.getBalance()).isEqualTo(9000L);
        assertThat(account.getDailyTransferLimit()).isEqualTo(1_000_000L);
        assertThat(account.getOneTimeTransferLimit()).isEqualTo(100_000L);
        assertThat(account.getStatus()).isEqualTo(AccountStatus.ACTIVE);
        assertThat(account.getIsDeleted()).isFalse();
    }
}
