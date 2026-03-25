package com.donttaz.donttazapi.account.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.donttaz.donttazapi.account.domain.Account;
import com.donttaz.donttazapi.account.domain.AccountStatus;
import com.donttaz.donttazapi.account.domain.AccountVerification;
import com.donttaz.donttazapi.account.domain.Transaction;
import com.donttaz.donttazapi.account.dto.request.AccountDepositRequest;
import com.donttaz.donttazapi.account.dto.request.AccountTransferLimitUpdateRequest;
import com.donttaz.donttazapi.account.dto.request.AccountTransferRequest;
import com.donttaz.donttazapi.account.dto.request.AccountWithdrawalRequest;
import com.donttaz.donttazapi.account.dto.request.OneWonVerificationConfirmRequest;
import com.donttaz.donttazapi.account.dto.request.OneWonVerificationRequest;
import com.donttaz.donttazapi.account.dto.response.AccountDepositResponse;
import com.donttaz.donttazapi.account.dto.response.AccountTransferLimitUpdateResponse;
import com.donttaz.donttazapi.account.dto.response.AccountTransferResponse;
import com.donttaz.donttazapi.account.dto.response.AccountWithdrawalResponse;
import com.donttaz.donttazapi.account.dto.response.OneWonVerificationConfirmResponse;
import com.donttaz.donttazapi.account.dto.response.OneWonVerificationRequestResponse;
import com.donttaz.donttazapi.account.repository.AccountRepository;
import com.donttaz.donttazapi.account.repository.AccountVerificationRepository;
import com.donttaz.donttazapi.account.repository.TransactionRepository;
import com.donttaz.donttazapi.account.service.dto.AccountDepositCommand;
import com.donttaz.donttazapi.account.service.dto.AccountDepositResult;
import com.donttaz.donttazapi.account.service.dto.AccountDetailResult;
import com.donttaz.donttazapi.account.service.dto.AccountWithdrawalCommand;
import com.donttaz.donttazapi.account.service.dto.AccountWithdrawalResult;
import com.donttaz.donttazapi.account.service.dto.OneWonVerificationConfirmCommand;
import com.donttaz.donttazapi.account.service.dto.OneWonVerificationConfirmResult;
import com.donttaz.donttazapi.account.service.dto.OneWonVerificationRequestCommand;
import com.donttaz.donttazapi.account.service.dto.OneWonVerificationRequestResult;
import com.donttaz.donttazapi.account.service.dto.TransferCommand;
import com.donttaz.donttazapi.account.service.dto.TransferLimitCommand;
import com.donttaz.donttazapi.account.service.dto.TransferResult;
import com.donttaz.donttazapi.account.service.dto.TransferTransactionResult;
import com.donttaz.donttazapi.common.exception.CustomException;
import com.donttaz.donttazapi.common.exception.ErrorCode;
import com.donttaz.donttazapi.common.security.CurrentUserProvider;
import com.donttaz.donttazapi.infra.banking.client.BankingClient;
import com.donttaz.donttazapi.infra.banking.config.FinApiProperties;
import com.donttaz.donttazapi.notification.service.PushNotificationService;
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
    private AccountVerificationRepository accountVerificationRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private CurrentUserProvider currentUserProvider;

    @Mock
    private FinApiProperties finApiProperties;

    @Mock
    private PushNotificationService pushNotificationService;

    @InjectMocks
    private AccountTransferService accountTransferService;

    @Test
    @DisplayName("deposit stores transaction history")
    void depositSuccess() {
        AccountDepositRequest request = new AccountDepositRequest("100000000", "deposit");
        Account account = createAccount("0016174648358792", 100000000L, 1L);
        given(currentUserProvider.getCurrentUserId()).willReturn(1L);
        given(bankingClient.deposit(any(Long.class), any(AccountDepositCommand.class)))
                .willReturn(new AccountDepositResult("59", "20240401"));
        given(accountSyncService.syncAccountSnapshot(1L, "0016174648358792")).willReturn(account);

        AccountDepositResponse response = accountTransferService.deposit("0016174648358792", request);

        ArgumentCaptor<AccountDepositCommand> commandCaptor = ArgumentCaptor.forClass(AccountDepositCommand.class);
        ArgumentCaptor<Transaction> transactionCaptor = ArgumentCaptor.forClass(Transaction.class);
        verify(bankingClient).deposit(org.mockito.ArgumentMatchers.eq(1L), commandCaptor.capture());
        verify(transactionRepository).save(transactionCaptor.capture());
        verify(pushNotificationService).sendDepositNotification(1L, "0016174648358792", 100000000L, "deposit");
        assertThat(commandCaptor.getValue().accountNo()).isEqualTo("0016174648358792");
        assertThat(commandCaptor.getValue().transactionBalance()).isEqualTo("100000000");
        assertThat(commandCaptor.getValue().transactionSummary()).isEqualTo("deposit");
        assertThat(transactionCaptor.getValue().getUserId()).isEqualTo(1L);
        assertThat(transactionCaptor.getValue().getSourceTxId()).isEqualTo("59");
        assertThat(transactionCaptor.getValue().getAmount()).isEqualTo(100000000L);
        assertThat(transactionCaptor.getValue().getTxTypeName()).isEqualTo("입금");
        assertThat(transactionCaptor.getValue().getCounterpartyName()).isEqualTo("본인");
        assertThat(response.transactionUniqueNo()).isEqualTo("59");
        assertThat(response.transactionDate()).isEqualTo("20240401");
    }

    @Test
    @DisplayName("deposit uses account owner when security context is missing")
    void depositWithoutSecurityContextUsesAccountOwner() {
        AccountDepositRequest request = new AccountDepositRequest("50000", "deposit");
        Account account = createAccount("0016174648358792", 150000L, 7L);
        given(currentUserProvider.getCurrentUserId())
                .willThrow(new CustomException(ErrorCode.AUTHENTICATION_REQUIRED));
        given(accountRepository.findFirstByAccountNumberAndStatusAndIsDeletedFalse(
                "0016174648358792",
                AccountStatus.ACTIVE
        )).willReturn(Optional.of(account));
        given(bankingClient.deposit(any(Long.class), any(AccountDepositCommand.class)))
                .willReturn(new AccountDepositResult("77", "20240402"));
        given(accountSyncService.syncAccountSnapshot(7L, "0016174648358792")).willReturn(account);

        AccountDepositResponse response = accountTransferService.deposit("0016174648358792", request);

        ArgumentCaptor<AccountDepositCommand> commandCaptor = ArgumentCaptor.forClass(AccountDepositCommand.class);
        verify(bankingClient).deposit(org.mockito.ArgumentMatchers.eq(7L), commandCaptor.capture());
        verify(accountSyncService).syncAccountSnapshot(7L, "0016174648358792");
        verify(pushNotificationService).sendDepositNotification(7L, "0016174648358792", 50000L, "deposit");
        assertThat(commandCaptor.getValue().accountNo()).isEqualTo("0016174648358792");
        assertThat(response.transactionUniqueNo()).isEqualTo("77");
    }

    @Test
    @DisplayName("withdrawal stores transaction history")
    void withdrawalSuccess() {
        AccountWithdrawalRequest request = new AccountWithdrawalRequest("100000", "withdrawal");
        Account account = createAccount("0016174648358792", 500000L, 1L);
        given(currentUserProvider.getCurrentUserId()).willReturn(1L);
        given(bankingClient.withdrawal(any(Long.class), any(AccountWithdrawalCommand.class)))
                .willReturn(new AccountWithdrawalResult("60", "20240401"));
        given(accountSyncService.syncAccountSnapshot(1L, "0016174648358792")).willReturn(account);

        AccountWithdrawalResponse response = accountTransferService.withdrawal("0016174648358792", request);

        ArgumentCaptor<AccountWithdrawalCommand> commandCaptor = ArgumentCaptor.forClass(AccountWithdrawalCommand.class);
        ArgumentCaptor<Transaction> transactionCaptor = ArgumentCaptor.forClass(Transaction.class);
        verify(bankingClient).withdrawal(org.mockito.ArgumentMatchers.eq(1L), commandCaptor.capture());
        verify(transactionRepository).save(transactionCaptor.capture());
        assertThat(commandCaptor.getValue().transactionSummary()).isEqualTo("withdrawal");
        assertThat(transactionCaptor.getValue().getUserId()).isEqualTo(1L);
        assertThat(transactionCaptor.getValue().getSourceTxId()).isEqualTo("60");
        assertThat(transactionCaptor.getValue().getTxTypeName()).isEqualTo("출금");
        assertThat(transactionCaptor.getValue().getCounterpartyName()).isEqualTo("본인");
        assertThat(response.transactionUniqueNo()).isEqualTo("60");
        assertThat(response.transactionDate()).isEqualTo("20240401");
    }

    @Test
    @DisplayName("transfer stores withdrawal and owned deposit transactions")
    void transferSuccess() {
        AccountTransferRequest request = new AccountTransferRequest(
                "0204667768182760",
                "deposit transfer",
                "10000000",
                "0016174648358792",
                "withdrawal transfer"
        );
        Account withdrawalAccount = createAccount("0016174648358792", 90000000L, 1L);
        Account depositAccount = createAccount("0204667768182760", 110000000L, 1L);
        TransferResult result = new TransferResult(
                List.of(
                        new TransferTransactionResult("61", "0016174648358792", "20240401", "2", "WITHDRAWAL_TRANSFER", "0204667768182760"),
                        new TransferTransactionResult("62", "0204667768182760", "20240401", "1", "DEPOSIT_TRANSFER", "0016174648358792")
                )
        );
        given(currentUserProvider.getCurrentUserId()).willReturn(1L);
        given(bankingClient.transfer(any(Long.class), any(TransferCommand.class))).willReturn(result);
        given(accountSyncService.syncAccountSnapshot(1L, "0016174648358792")).willReturn(withdrawalAccount);
        given(accountRepository.findByUserIdAndAccountNumberAndIsDeletedFalse(1L, "0204667768182760"))
                .willReturn(Optional.of(depositAccount));
        given(accountSyncService.syncAccountSnapshot(1L, "0204667768182760")).willReturn(depositAccount);

        AccountTransferResponse response = accountTransferService.transfer(request);

        ArgumentCaptor<TransferCommand> commandCaptor = ArgumentCaptor.forClass(TransferCommand.class);
        ArgumentCaptor<Transaction> transactionCaptor = ArgumentCaptor.forClass(Transaction.class);
        verify(bankingClient).transfer(org.mockito.ArgumentMatchers.eq(1L), commandCaptor.capture());
        verify(transactionRepository, times(2)).save(transactionCaptor.capture());
        assertThat(commandCaptor.getValue().depositAccountNo()).isEqualTo("0204667768182760");
        assertThat(commandCaptor.getValue().withdrawalAccountNo()).isEqualTo("0016174648358792");
        assertThat(commandCaptor.getValue().transactionBalance()).isEqualTo("10000000");
        assertThat(transactionCaptor.getAllValues())
                .extracting(Transaction::getCounterpartyName)
                .containsOnly("이체");
        assertThat(transactionCaptor.getAllValues())
                .extracting(Transaction::getTxTypeName)
                .containsExactlyInAnyOrder("이체 출금", "이체 입금");
        assertThat(response.transactions()).hasSize(2);
    }

    @Test
    @DisplayName("transfer uses withdrawal account owner when security context is missing")
    void transferWithoutSecurityContextUsesAccountOwner() {
        AccountTransferRequest request = new AccountTransferRequest(
                "0204667768182760",
                "deposit transfer",
                "10000000",
                "0016174648358792",
                "withdrawal transfer"
        );
        Account withdrawalAccount = createAccount("0016174648358792", 90000000L, 9L);
        Account depositAccount = createAccount("0204667768182760", 110000000L, 9L);
        TransferResult result = new TransferResult(
                List.of(
                        new TransferTransactionResult("61", "0016174648358792", "20240401", "2", "WITHDRAWAL_TRANSFER", "0204667768182760"),
                        new TransferTransactionResult("62", "0204667768182760", "20240401", "1", "DEPOSIT_TRANSFER", "0016174648358792")
                )
        );
        given(currentUserProvider.getCurrentUserId())
                .willThrow(new CustomException(ErrorCode.AUTHENTICATION_REQUIRED));
        given(accountRepository.findFirstByAccountNumberAndStatusAndIsDeletedFalse(
                "0016174648358792",
                AccountStatus.ACTIVE
        )).willReturn(Optional.of(withdrawalAccount));
        given(bankingClient.transfer(any(Long.class), any(TransferCommand.class))).willReturn(result);
        given(accountSyncService.syncAccountSnapshot(9L, "0016174648358792")).willReturn(withdrawalAccount);
        given(accountRepository.findByUserIdAndAccountNumberAndIsDeletedFalse(9L, "0204667768182760"))
                .willReturn(Optional.of(depositAccount));
        given(accountSyncService.syncAccountSnapshot(9L, "0204667768182760")).willReturn(depositAccount);

        AccountTransferResponse response = accountTransferService.transfer(request);

        ArgumentCaptor<TransferCommand> commandCaptor = ArgumentCaptor.forClass(TransferCommand.class);
        verify(bankingClient).transfer(org.mockito.ArgumentMatchers.eq(9L), commandCaptor.capture());
        assertThat(commandCaptor.getValue().withdrawalAccountNo()).isEqualTo("0016174648358792");
        assertThat(response.transactions()).hasSize(2);
    }

    @Test
    @DisplayName("updateTransferLimit syncs account snapshot")
    void updateTransferLimitSuccess() {
        AccountTransferLimitUpdateRequest request = new AccountTransferLimitUpdateRequest("20000000", "100000000");
        AccountDetailResult result = new AccountDetailResult(
                "001",
                "BANK",
                "USER",
                "0016174648358792",
                "Demand Deposit",
                "1",
                "DEPOSIT",
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
        assertThat(response.accountNo()).isEqualTo("0016174648358792");
    }

    @Test
    @DisplayName("requestOneWonVerification saves verification history")
    void requestOneWonVerificationSuccess() {
        OneWonVerificationRequest request = new OneWonVerificationRequest("0011214764051239");
        given(currentUserProvider.getCurrentUserId()).willReturn(1L);
        given(finApiProperties.getOneWonAuthText()).willReturn("DONTTAZ");
        given(finApiProperties.getInstitutionCode()).willReturn("00100");
        given(finApiProperties.getOneWonExpirationSeconds()).willReturn(300);
        given(bankingClient.requestOneWonVerification(any(Long.class), any(OneWonVerificationRequestCommand.class)))
                .willReturn(new OneWonVerificationRequestResult("7", "0011214764051239"));

        OneWonVerificationRequestResponse response = accountTransferService.requestOneWonVerification(request);

        ArgumentCaptor<OneWonVerificationRequestCommand> commandCaptor = ArgumentCaptor.forClass(OneWonVerificationRequestCommand.class);
        ArgumentCaptor<AccountVerification> verificationCaptor = ArgumentCaptor.forClass(AccountVerification.class);
        verify(bankingClient).requestOneWonVerification(org.mockito.ArgumentMatchers.eq(1L), commandCaptor.capture());
        verify(accountVerificationRepository).save(verificationCaptor.capture());
        assertThat(commandCaptor.getValue().accountNo()).isEqualTo("0011214764051239");
        assertThat(commandCaptor.getValue().authText()).isEqualTo("DONTTAZ");
        assertThat(verificationCaptor.getValue().getVerificationId()).isEqualTo("7");
        assertThat(response.verificationId()).isEqualTo("7");
        assertThat(response.accountNo()).isEqualTo("0011214764051239");
    }

    @Test
    @DisplayName("confirmOneWonVerification marks verification as verified")
    void confirmOneWonVerificationSuccess() {
        OneWonVerificationConfirmRequest request = new OneWonVerificationConfirmRequest("7", "8212");
        AccountVerification verification = AccountVerification.builder()
                .verificationId("7")
                .userId(1L)
                .bankCode("00100")
                .accountNumber("0011214764051239")
                .authCode(null)
                .expiredAt(LocalDateTime.now().plusMinutes(5))
                .verified(false)
                .consentToken(null)
                .build();
        given(currentUserProvider.getCurrentUserId()).willReturn(1L);
        given(finApiProperties.getOneWonAuthText()).willReturn("DONTTAZ");
        given(accountVerificationRepository.findByVerificationIdAndUserId("7", 1L))
                .willReturn(Optional.of(verification));
        given(bankingClient.confirmOneWonVerification(any(Long.class), any(OneWonVerificationConfirmCommand.class)))
                .willReturn(new OneWonVerificationConfirmResult("SUCCESS", true, "7", "0011214764051239"));

        OneWonVerificationConfirmResponse response = accountTransferService.confirmOneWonVerification(request);

        ArgumentCaptor<OneWonVerificationConfirmCommand> commandCaptor = ArgumentCaptor.forClass(OneWonVerificationConfirmCommand.class);
        verify(bankingClient).confirmOneWonVerification(org.mockito.ArgumentMatchers.eq(1L), commandCaptor.capture());
        assertThat(commandCaptor.getValue().accountNo()).isEqualTo("0011214764051239");
        assertThat(commandCaptor.getValue().authText()).isEqualTo("DONTTAZ");
        assertThat(commandCaptor.getValue().authCode()).isEqualTo("8212");
        assertThat(verification.isVerified()).isTrue();
        assertThat(response.status()).isEqualTo("SUCCESS");
        assertThat(response.verified()).isTrue();
    }

    private Account createAccount(String accountNumber, Long balance, Long userId) {
        return Account.builder()
                .userId(userId)
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

