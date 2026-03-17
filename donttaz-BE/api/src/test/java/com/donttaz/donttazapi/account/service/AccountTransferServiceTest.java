package com.donttaz.donttazapi.account.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.donttaz.donttazapi.account.domain.Account;
import com.donttaz.donttazapi.account.domain.AccountStatus;
import com.donttaz.donttazapi.account.domain.Transaction;
import com.donttaz.donttazapi.account.dto.request.AccountDepositRequest;
import com.donttaz.donttazapi.account.dto.request.AccountTransferRequest;
import com.donttaz.donttazapi.account.dto.request.AccountTransferLimitUpdateRequest;
import com.donttaz.donttazapi.account.dto.request.AccountWithdrawalRequest;
import com.donttaz.donttazapi.account.dto.response.AccountDepositResponse;
import com.donttaz.donttazapi.account.dto.response.AccountTransferResponse;
import com.donttaz.donttazapi.account.dto.response.AccountTransferLimitUpdateResponse;
import com.donttaz.donttazapi.account.dto.response.AccountWithdrawalResponse;
import com.donttaz.donttazapi.account.repository.AccountRepository;
import com.donttaz.donttazapi.account.repository.TransactionRepository;
import com.donttaz.donttazapi.account.service.dto.AccountDepositCommand;
import com.donttaz.donttazapi.account.service.dto.AccountDepositResult;
import com.donttaz.donttazapi.account.service.dto.AccountDetailResult;
import com.donttaz.donttazapi.account.service.dto.AccountWithdrawalCommand;
import com.donttaz.donttazapi.account.service.dto.AccountWithdrawalResult;
import com.donttaz.donttazapi.account.service.dto.TransferCommand;
import com.donttaz.donttazapi.account.service.dto.TransferLimitCommand;
import com.donttaz.donttazapi.account.service.dto.TransferResult;
import com.donttaz.donttazapi.account.service.dto.TransferTransactionResult;
import com.donttaz.donttazapi.common.security.CurrentUserProvider;
import com.donttaz.donttazapi.infra.banking.client.BankingClient;
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
class AccountTransferServiceTest {

        @Mock
        private BankingClient bankingClient;

        @Mock
        private AccountSyncService accountSyncService;

    @Mock
    private AccountRepository accountRepository;

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
        Account account = createAccount("0016174648358792", 100000000L);
        given(currentUserProvider.getCurrentUserId()).willReturn(1L);
        given(bankingClient.deposit(any(Long.class), any(AccountDepositCommand.class)))
                .willReturn(new AccountDepositResult("59", "20240401"));
        given(accountSyncService.syncAccountSnapshot("0016174648358792")).willReturn(account);

                AccountDepositResponse response = accountTransferService.deposit("0016174648358792", request);

                ArgumentCaptor<AccountDepositCommand> commandCaptor = ArgumentCaptor
                                .forClass(AccountDepositCommand.class);
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
        Account account = createAccount("0016174648358792", 500000L);
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

    @Test
    @DisplayName("계좌 이체 요청 시 외부 호출 후 출금/입금 거래를 저장한다.")
    void transferSuccess() {
        AccountTransferRequest request = new AccountTransferRequest(
                "0204667768182760",
                "deposit transfer",
                "10000000",
                "0016174648358792",
                "withdrawal transfer"
        );
        Account withdrawalAccount = createAccount("0016174648358792", 90000000L);
        Account depositAccount = createAccount("0204667768182760", 110000000L);
        TransferResult result = new TransferResult(
                List.of(
                        new TransferTransactionResult("61", "0016174648358792", "20240401", "2", "출금(이체)", "0204667768182760"),
                        new TransferTransactionResult("62", "0204667768182760", "20240401", "1", "입금(이체)", "0016174648358792")
                )
        );
        given(currentUserProvider.getCurrentUserId()).willReturn(1L);
        given(bankingClient.transfer(any(Long.class), any(TransferCommand.class))).willReturn(result);
        given(accountSyncService.syncAccountSnapshot("0016174648358792")).willReturn(withdrawalAccount);
        given(accountRepository.findByUserIdAndAccountNumberAndIsDeletedFalse(1L, "0204667768182760"))
                .willReturn(Optional.of(depositAccount));
        given(accountSyncService.syncAccountSnapshot("0204667768182760")).willReturn(depositAccount);

        AccountTransferResponse response = accountTransferService.transfer(request);

        ArgumentCaptor<TransferCommand> commandCaptor = ArgumentCaptor.forClass(TransferCommand.class);
        verify(bankingClient).transfer(org.mockito.ArgumentMatchers.eq(1L), commandCaptor.capture());
        verify(transactionRepository, times(2)).save(any(Transaction.class));
        assertThat(commandCaptor.getValue().depositAccountNo()).isEqualTo("0204667768182760");
        assertThat(commandCaptor.getValue().withdrawalAccountNo()).isEqualTo("0016174648358792");
        assertThat(commandCaptor.getValue().transactionBalance()).isEqualTo("10000000");
        assertThat(response.transactions()).hasSize(2);
        assertThat(response.transactions().get(0).transactionUniqueNo()).isEqualTo("61");
        assertThat(response.transactions().get(1).transactionUniqueNo()).isEqualTo("62");
    }

    @Test
    @DisplayName("이체 한도 변경 요청 시 외부 API 호출 후 계좌 스냅샷을 동기화한다")
    void updateTransferLimitSuccess() {
        AccountTransferLimitUpdateRequest request = new AccountTransferLimitUpdateRequest("20000000", "100000000");
        AccountDetailResult result = new AccountDetailResult(
                "001",
                "BANK",
                "USER",
                "0016174648358792",
                "입출금 상품",
                "1",
                "수시입출금",
                "20240401",
                "20290401",
                "100000000",
                "20000000",
                "89900000",
                "20240401",
                "KRW"
        );
        given(currentUserProvider.getCurrentUserId()).willReturn(1L);
        given(bankingClient.updateTransferLimit(any(Long.class), any(TransferLimitCommand.class))).willReturn(result);

        AccountTransferLimitUpdateResponse response = accountTransferService.updateTransferLimit("0016174648358792", request);

        ArgumentCaptor<TransferLimitCommand> commandCaptor = ArgumentCaptor.forClass(TransferLimitCommand.class);
        verify(bankingClient).updateTransferLimit(org.mockito.ArgumentMatchers.eq(1L), commandCaptor.capture());
        verify(accountSyncService).syncAccountDetailSnapshot(result);
        assertThat(commandCaptor.getValue().accountNo()).isEqualTo("0016174648358792");
        assertThat(commandCaptor.getValue().oneTimeTransferLimit()).isEqualTo("20000000");
        assertThat(commandCaptor.getValue().dailyTransferLimit()).isEqualTo("100000000");
        assertThat(response.accountNo()).isEqualTo("0016174648358792");
        assertThat(response.oneTimeTransferLimit()).isEqualTo("20000000");
        assertThat(response.dailyTransferLimit()).isEqualTo("100000000");
    }

    private Account createAccount(String accountNumber, Long balance) {
        return Account.builder()
                .userId(1L)
                .bankCode("001")
                .bankName("BANK")
                .accountNumber(accountNumber)
                .balance(balance)
                .linkedAt(LocalDateTime.now())
                .lastSyncedAt(LocalDateTime.now())
                .isDeleted(Boolean.FALSE)
                .status(AccountStatus.ACTIVE)
                .build();
    }
}
