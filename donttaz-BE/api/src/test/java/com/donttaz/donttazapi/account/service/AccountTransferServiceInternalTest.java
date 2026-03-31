package com.donttaz.donttazapi.account.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.donttaz.donttazapi.account.domain.Account;
import com.donttaz.donttazapi.account.domain.AccountStatus;
import com.donttaz.donttazapi.account.domain.Transaction;
import com.donttaz.donttazapi.account.dto.request.AccountTransferRequest;
import com.donttaz.donttazapi.account.dto.response.AccountTransferResponse;
import com.donttaz.donttazapi.account.repository.AccountRepository;
import com.donttaz.donttazapi.account.repository.AccountVerificationRepository;
import com.donttaz.donttazapi.account.repository.TransactionRepository;
import com.donttaz.donttazapi.account.service.dto.TransferCommand;
import com.donttaz.donttazapi.account.service.dto.TransferResult;
import com.donttaz.donttazapi.account.service.dto.TransferTransactionResult;
import com.donttaz.donttazapi.common.security.CurrentUserProvider;
import com.donttaz.donttazapi.infra.banking.client.BankingClient;
import com.donttaz.donttazapi.infra.banking.config.FinApiProperties;
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
class AccountTransferServiceInternalTest {

    @Mock
    private BankingClient bankingClient;

    @Mock
    private AccountSyncService accountSyncService;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private AccountVerificationRepository accountVerificationRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private CurrentUserProvider currentUserProvider;

    @Mock
    private FinApiProperties finApiProperties;

    @InjectMocks
    private AccountTransferService accountTransferService;

    @Test
    @DisplayName("transfer(userId, request) executes without security context")
    void transferWithExplicitUserIdSuccess() {
        AccountTransferRequest request = new AccountTransferRequest(
                "0204667768182760",
                "PAYMENT_SETTLEMENT",
                "10000",
                "0016174648358792",
                "EMART"
        );
        Account withdrawalAccount = Account.builder()
                .userId(7L)
                .bankCode("001")
                .bankName("BANK")
                .accountNumber("0016174648358792")
                .balance(490_000L)
                .linkedAt(LocalDateTime.now())
                .lastSyncedAt(LocalDateTime.now())
                .isDeleted(Boolean.FALSE)
                .status(AccountStatus.ACTIVE)
                .build();
        TransferResult result = new TransferResult(List.of(
                new TransferTransactionResult("61", "0016174648358792", "20240401", "2", "WITHDRAWAL_TRANSFER", "0204667768182760"),
                new TransferTransactionResult("62", "0204667768182760", "20240401", "1", "DEPOSIT_TRANSFER", "0016174648358792")
        ));

        given(bankingClient.transfer(any(Long.class), any(TransferCommand.class))).willReturn(result);
        given(accountSyncService.syncAccountSnapshot(7L, "0016174648358792")).willReturn(withdrawalAccount);
        given(accountRepository.findFirstByAccountNumberAndStatusAndIsDeletedFalse("0204667768182760", AccountStatus.ACTIVE))
                .willReturn(Optional.empty());

        AccountTransferResponse response = accountTransferService.transfer(7L, request);

        ArgumentCaptor<TransferCommand> commandCaptor = ArgumentCaptor.forClass(TransferCommand.class);
        ArgumentCaptor<Transaction> transactionCaptor = ArgumentCaptor.forClass(Transaction.class);
        verify(bankingClient).transfer(org.mockito.ArgumentMatchers.eq(7L), commandCaptor.capture());
        verify(currentUserProvider, never()).getCurrentUserId();
        verify(transactionRepository).save(transactionCaptor.capture());
        assertThat(commandCaptor.getValue().withdrawalTransactionSummary()).isEqualTo("EMART");
        assertThat(transactionCaptor.getValue().getCounterpartyName()).isEqualTo("EMART");
        assertThat(transactionCaptor.getValue().getTransactionSummary()).isEqualTo("EMART");
        assertThat(response.transactions()).hasSize(2);
    }
}
