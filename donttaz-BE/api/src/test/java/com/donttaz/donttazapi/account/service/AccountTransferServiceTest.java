package com.donttaz.donttazapi.account.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.donttaz.donttazapi.account.domain.Account;
import com.donttaz.donttazapi.account.domain.AccountStatus;
import com.donttaz.donttazapi.account.domain.Transaction;
import com.donttaz.donttazapi.account.dto.request.AccountDepositRequest;
import com.donttaz.donttazapi.account.dto.request.AccountWithdrawalRequest;
import com.donttaz.donttazapi.account.dto.response.AccountDepositResponse;
import com.donttaz.donttazapi.account.dto.response.AccountWithdrawalResponse;
import com.donttaz.donttazapi.account.repository.TransactionRepository;
import com.donttaz.donttazapi.account.service.dto.AccountDepositCommand;
import com.donttaz.donttazapi.account.service.dto.AccountDepositResult;
import com.donttaz.donttazapi.account.service.dto.AccountWithdrawalCommand;
import com.donttaz.donttazapi.account.service.dto.AccountWithdrawalResult;
import com.donttaz.donttazapi.common.security.CurrentUserProvider;
import com.donttaz.donttazapi.infra.banking.client.BankingClient;
import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AccountTransferServiceTest {

    @Mock
    private BankingClient bankingClient;

    @Mock
    private AccountSyncService accountSyncService;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private CurrentUserProvider currentUserProvider;

    @InjectMocks
    private AccountTransferService accountTransferService;

    @Test
    @DisplayName("입금 요청 시 외부 호출 후 내부 거래 이력을 저장한다.")
    void depositSuccess() {
        AccountDepositRequest request = new AccountDepositRequest("100000000", "deposit");
        Account account = Account.builder()
                .userId(1L)
                .bankCode("001")
                .bankName("BANK")
                .accountNumber("0016174648358792")
                .balance(100000000L)
                .linkedAt(LocalDateTime.now())
                .lastSyncedAt(LocalDateTime.now())
                .isDeleted(Boolean.FALSE)
                .status(AccountStatus.ACTIVE)
                .build();
        given(currentUserProvider.getCurrentUserId()).willReturn(1L);
        given(bankingClient.deposit(any(Long.class), any(AccountDepositCommand.class)))
                .willReturn(new AccountDepositResult("59", "20240401"));
        given(accountSyncService.syncAccountSnapshot("0016174648358792")).willReturn(account);

        AccountDepositResponse response = accountTransferService.deposit("0016174648358792", request);

        ArgumentCaptor<AccountDepositCommand> commandCaptor = ArgumentCaptor.forClass(AccountDepositCommand.class);
        ArgumentCaptor<Transaction> transactionCaptor = ArgumentCaptor.forClass(Transaction.class);
        verify(bankingClient).deposit(org.mockito.ArgumentMatchers.eq(1L), commandCaptor.capture());
        verify(transactionRepository).save(transactionCaptor.capture());
        assertThat(commandCaptor.getValue().accountNo()).isEqualTo("0016174648358792");
        assertThat(commandCaptor.getValue().transactionBalance()).isEqualTo("100000000");
        assertThat(commandCaptor.getValue().transactionSummary()).isEqualTo("deposit");
        assertThat(transactionCaptor.getValue().getAmount()).isEqualTo(100000000);
        assertThat(transactionCaptor.getValue().getBalanceAfter()).isEqualTo(100000000);
        assertThat(transactionCaptor.getValue().getMemo()).isEqualTo("deposit");
        assertThat(transactionCaptor.getValue().getCategory()).isEqualTo("DEPOSIT");
        assertThat(response.transactionUniqueNo()).isEqualTo("59");
        assertThat(response.transactionDate()).isEqualTo("20240401");
    }

    @Test
    @DisplayName("출금 요청 시 외부 호출 후 내부 거래 이력을 저장한다.")
    void withdrawalSuccess() {
        AccountWithdrawalRequest request = new AccountWithdrawalRequest("100000", "withdrawal");
        Account account = Account.builder()
                .userId(1L)
                .bankCode("001")
                .bankName("BANK")
                .accountNumber("0016174648358792")
                .balance(500000L)
                .linkedAt(LocalDateTime.now())
                .lastSyncedAt(LocalDateTime.now())
                .isDeleted(Boolean.FALSE)
                .status(AccountStatus.ACTIVE)
                .build();
        given(currentUserProvider.getCurrentUserId()).willReturn(1L);
        given(bankingClient.withdrawal(any(Long.class), any(AccountWithdrawalCommand.class)))
                .willReturn(new AccountWithdrawalResult("60", "20240401"));
        given(accountSyncService.syncAccountSnapshot("0016174648358792")).willReturn(account);

        AccountWithdrawalResponse response = accountTransferService.withdrawal("0016174648358792", request);

        ArgumentCaptor<AccountWithdrawalCommand> commandCaptor = ArgumentCaptor.forClass(AccountWithdrawalCommand.class);
        ArgumentCaptor<Transaction> transactionCaptor = ArgumentCaptor.forClass(Transaction.class);
        verify(bankingClient).withdrawal(org.mockito.ArgumentMatchers.eq(1L), commandCaptor.capture());
        verify(transactionRepository).save(transactionCaptor.capture());
        assertThat(commandCaptor.getValue().accountNo()).isEqualTo("0016174648358792");
        assertThat(commandCaptor.getValue().transactionBalance()).isEqualTo("100000");
        assertThat(commandCaptor.getValue().transactionSummary()).isEqualTo("withdrawal");
        assertThat(transactionCaptor.getValue().getAmount()).isEqualTo(100000);
        assertThat(transactionCaptor.getValue().getBalanceAfter()).isEqualTo(500000);
        assertThat(transactionCaptor.getValue().getMemo()).isEqualTo("withdrawal");
        assertThat(transactionCaptor.getValue().getCategory()).isEqualTo("WITHDRAWAL");
        assertThat(response.transactionUniqueNo()).isEqualTo("60");
        assertThat(response.transactionDate()).isEqualTo("20240401");
    }
}
