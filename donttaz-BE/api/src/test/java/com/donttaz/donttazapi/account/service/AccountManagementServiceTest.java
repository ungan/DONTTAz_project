package com.donttaz.donttazapi.account.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.donttaz.donttazapi.account.domain.Account;
import com.donttaz.donttazapi.account.domain.AccountStatus;
import com.donttaz.donttazapi.account.dto.request.AccountCloseRequest;
import com.donttaz.donttazapi.account.dto.response.AccountCloseResponse;
import com.donttaz.donttazapi.account.repository.AccountRepository;
import com.donttaz.donttazapi.account.service.dto.AccountCloseCommand;
import com.donttaz.donttazapi.account.service.dto.AccountCloseResult;
import com.donttaz.donttazapi.common.exception.CustomException;
import com.donttaz.donttazapi.common.exception.ErrorCode;
import com.donttaz.donttazapi.common.security.CurrentUserProvider;
import com.donttaz.donttazapi.infra.banking.client.BankingClient;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AccountManagementServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private BankingClient bankingClient;

    @Mock
    private CurrentUserProvider currentUserProvider;

    @InjectMocks
    private AccountManagementService accountManagementService;

    @Test
    @DisplayName("주계좌 설정 시 기존 주계좌를 해제하고 새 계좌를 지정한다")
    void setPrimaryAccountSuccess() {
        Account account = activeAccount(1L, "001", "001123456789", 1000L, false);
        given(currentUserProvider.getCurrentUserId()).willReturn(1L);
        given(accountRepository.findAllByUserIdAndAccountNumberAndStatusAndIsDeletedFalse(
                1L,
                "001123456789",
                AccountStatus.ACTIVE
        )).willReturn(List.of(account));

        accountManagementService.setPrimaryAccount("001123456789");

        verify(accountRepository).clearPrimaryByUserId(1L);
        assertThat(account.getIsPrimary()).isTrue();
    }

    @Test
    @DisplayName("주계좌 설정 대상이 없으면 ACCOUNT_NOT_FOUND를 던진다")
    void setPrimaryAccountFailsWhenAccountNotFound() {
        given(currentUserProvider.getCurrentUserId()).willReturn(1L);
        given(accountRepository.findAllByUserIdAndAccountNumberAndStatusAndIsDeletedFalse(
                1L,
                "001123456789",
                AccountStatus.ACTIVE
        )).willReturn(List.of());

        CustomException exception = assertThrows(
                CustomException.class,
                () -> accountManagementService.setPrimaryAccount("001123456789")
        );

        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.ACCOUNT_NOT_FOUND);
    }

    @Test
    @DisplayName("같은 계좌번호가 여러 개면 주계좌 설정을 거부한다")
    void setPrimaryAccountFailsWhenMultipleAccountsMatch() {
        Account first = activeAccount(1L, "001", "001123456789", 1000L, false);
        Account second = activeAccount(1L, "020", "001123456789", 2000L, false);
        given(currentUserProvider.getCurrentUserId()).willReturn(1L);
        given(accountRepository.findAllByUserIdAndAccountNumberAndStatusAndIsDeletedFalse(
                1L,
                "001123456789",
                AccountStatus.ACTIVE
        )).willReturn(List.of(first, second));

        CustomException exception = assertThrows(
                CustomException.class,
                () -> accountManagementService.setPrimaryAccount("001123456789")
        );

        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.INVALID_REQUEST);
    }

    @Test
    @DisplayName("계좌 해지 성공 시 exact lookup 후 계좌를 UNLINKED로 전환한다")
    void closeAccountSuccess() {
        Account targetAccount = activeAccount(1L, "001", "001123456789", 1000L, true);
        Account refundAccount = activeAccount(1L, "032", "0324003842129948", 5000L, false);
        AccountCloseRequest request = new AccountCloseRequest("032", "0324003842129948");
        AccountCloseResult result = new AccountCloseResult("CLOSED", "001123456789", "0324003842129948", "0");

        given(currentUserProvider.getCurrentUserId()).willReturn(1L);
        given(accountRepository.findByUserIdAndBankCodeAndAccountNumberAndStatusAndIsDeletedFalse(
                1L,
                "001",
                "001123456789",
                AccountStatus.ACTIVE
        )).willReturn(Optional.of(targetAccount));
        given(accountRepository.findByUserIdAndBankCodeAndAccountNumberAndStatusAndIsDeletedFalse(
                1L,
                "032",
                "0324003842129948",
                AccountStatus.ACTIVE
        )).willReturn(Optional.of(refundAccount));
        given(bankingClient.closeAccount(any(Long.class), any(AccountCloseCommand.class))).willReturn(result);

        AccountCloseResponse response = accountManagementService.closeAccount("001", "001123456789", request);

        verify(accountRepository).findByUserIdAndBankCodeAndAccountNumberAndStatusAndIsDeletedFalse(
                1L,
                "001",
                "001123456789",
                AccountStatus.ACTIVE
        );
        verify(accountRepository).save(targetAccount);
        assertThat(targetAccount.getStatus()).isEqualTo(AccountStatus.UNLINKED);
        assertThat(targetAccount.getIsPrimary()).isFalse();
        assertThat(targetAccount.getBalance()).isZero();
        assertThat(response.status()).isEqualTo("CLOSED");
        assertThat(response.accountNo()).isEqualTo("001123456789");
        assertThat(response.refundAccountNo()).isEqualTo("0324003842129948");
        assertThat(response.accountBalance()).isEqualTo("0");
    }

    @Test
    @DisplayName("계좌 해지 대상이 없으면 ACCOUNT_NOT_FOUND를 던진다")
    void closeAccountFailsWhenTargetAccountNotFound() {
        given(currentUserProvider.getCurrentUserId()).willReturn(1L);
        given(accountRepository.findByUserIdAndBankCodeAndAccountNumberAndStatusAndIsDeletedFalse(
                1L,
                "001",
                "001123456789",
                AccountStatus.ACTIVE
        )).willReturn(Optional.empty());

        CustomException exception = assertThrows(
                CustomException.class,
                () -> accountManagementService.closeAccount(
                        "001",
                        "001123456789",
                        new AccountCloseRequest("032", "0324003842129948")
                )
        );

        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.ACCOUNT_NOT_FOUND);
    }

    @Test
    @DisplayName("환급 계좌가 현재 사용자 활성 계좌가 아니면 ACCOUNT_NOT_FOUND를 던진다")
    void closeAccountFailsWhenRefundAccountNotFound() {
        Account targetAccount = activeAccount(1L, "001", "001123456789", 1000L, true);
        given(currentUserProvider.getCurrentUserId()).willReturn(1L);
        given(accountRepository.findByUserIdAndBankCodeAndAccountNumberAndStatusAndIsDeletedFalse(
                1L,
                "001",
                "001123456789",
                AccountStatus.ACTIVE
        )).willReturn(Optional.of(targetAccount));
        given(accountRepository.findByUserIdAndBankCodeAndAccountNumberAndStatusAndIsDeletedFalse(
                1L,
                "032",
                "0324003842129948",
                AccountStatus.ACTIVE
        )).willReturn(Optional.empty());

        CustomException exception = assertThrows(
                CustomException.class,
                () -> accountManagementService.closeAccount(
                        "001",
                        "001123456789",
                        new AccountCloseRequest("032", "0324003842129948")
                )
        );

        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.ACCOUNT_NOT_FOUND);
    }

    @Test
    @DisplayName("환급 계좌가 해지 대상과 같으면 INVALID_REQUEST를 던진다")
    void closeAccountFailsWhenRefundAccountMatchesTarget() {
        Account targetAccount = activeAccount(1L, "001", "001123456789", 1000L, true);
        AccountCloseRequest request = new AccountCloseRequest("001", "001123456789");

        given(currentUserProvider.getCurrentUserId()).willReturn(1L);
        given(accountRepository.findByUserIdAndBankCodeAndAccountNumberAndStatusAndIsDeletedFalse(
                1L,
                "001",
                "001123456789",
                AccountStatus.ACTIVE
        )).willReturn(Optional.of(targetAccount));

        CustomException exception = assertThrows(
                CustomException.class,
                () -> accountManagementService.closeAccount("001", "001123456789", request)
        );

        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.INVALID_REQUEST);
        verify(bankingClient, never()).closeAccount(any(Long.class), any(AccountCloseCommand.class));
    }

    @Test
    @DisplayName("해지 응답 잔액이 비정상이면 FIN_API_REQUEST_FAILED를 던지고 상태를 유지한다")
    void closeAccountFailsWhenCloseBalanceIsInvalid() {
        Account targetAccount = activeAccount(1L, "001", "001123456789", 1000L, true);
        Account refundAccount = activeAccount(1L, "032", "0324003842129948", 5000L, false);
        AccountCloseRequest request = new AccountCloseRequest("032", "0324003842129948");
        AccountCloseResult result = new AccountCloseResult("CLOSED", "001123456789", "0324003842129948", "abc");

        given(currentUserProvider.getCurrentUserId()).willReturn(1L);
        given(accountRepository.findByUserIdAndBankCodeAndAccountNumberAndStatusAndIsDeletedFalse(
                1L,
                "001",
                "001123456789",
                AccountStatus.ACTIVE
        )).willReturn(Optional.of(targetAccount));
        given(accountRepository.findByUserIdAndBankCodeAndAccountNumberAndStatusAndIsDeletedFalse(
                1L,
                "032",
                "0324003842129948",
                AccountStatus.ACTIVE
        )).willReturn(Optional.of(refundAccount));
        given(bankingClient.closeAccount(any(Long.class), any(AccountCloseCommand.class))).willReturn(result);

        CustomException exception = assertThrows(
                CustomException.class,
                () -> accountManagementService.closeAccount("001", "001123456789", request)
        );

        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.FIN_API_REQUEST_FAILED);
        assertThat(targetAccount.getStatus()).isEqualTo(AccountStatus.ACTIVE);
        assertThat(targetAccount.getBalance()).isEqualTo(1000L);
        assertThat(targetAccount.getIsPrimary()).isTrue();
        verify(accountRepository, never()).save(targetAccount);
    }

    private Account activeAccount(Long userId, String bankCode, String accountNumber, Long balance, boolean isPrimary) {
        return Account.builder()
                .userId(userId)
                .bankCode(bankCode)
                .bankName("BANK")
                .accountNumber(accountNumber)
                .balance(balance)
                .linkedAt(LocalDateTime.now())
                .lastSyncedAt(LocalDateTime.now())
                .isDeleted(Boolean.FALSE)
                .isPrimary(isPrimary)
                .status(AccountStatus.ACTIVE)
                .build();
    }
}
