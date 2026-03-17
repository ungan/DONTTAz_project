package com.donttaz.donttazapi.account.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.donttaz.donttazapi.account.domain.Account;
import com.donttaz.donttazapi.account.domain.AccountStatus;
import com.donttaz.donttazapi.account.repository.AccountRepository;
import com.donttaz.donttazapi.common.exception.CustomException;
import com.donttaz.donttazapi.common.exception.ErrorCode;
import com.donttaz.donttazapi.common.security.CurrentUserProvider;
import java.time.LocalDateTime;
import java.util.List;
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
    private CurrentUserProvider currentUserProvider;

    @InjectMocks
    private AccountManagementService accountManagementService;

    @Test
    @DisplayName("대표계좌 설정 시 기존 대표계좌를 해제하고 대상 계좌를 대표계좌로 설정한다")
    void setPrimaryAccountSuccess() {
        Account account = Account.builder()
                .userId(1L)
                .bankCode("001")
                .bankName("BANK")
                .accountNumber("001123456789")
                .balance(1000L)
                .linkedAt(LocalDateTime.now())
                .lastSyncedAt(LocalDateTime.now())
                .isDeleted(Boolean.FALSE)
                .isPrimary(Boolean.FALSE)
                .status(AccountStatus.ACTIVE)
                .build();
        given(currentUserProvider.getCurrentUserId()).willReturn(1L);
        given(accountRepository.findAllByUserIdAndAccountNumberAndStatusAndIsDeletedFalse(1L, "001123456789", AccountStatus.ACTIVE))
                .willReturn(List.of(account));

        accountManagementService.setPrimaryAccount("001123456789");

        verify(accountRepository).clearPrimaryByUserId(1L);
        assertThat(account.getIsPrimary()).isTrue();
    }

    @Test
    @DisplayName("대표계좌 설정 대상이 없으면 예외를 던진다")
    void setPrimaryAccountFailsWhenAccountNotFound() {
        given(currentUserProvider.getCurrentUserId()).willReturn(1L);
        given(accountRepository.findAllByUserIdAndAccountNumberAndStatusAndIsDeletedFalse(1L, "001123456789", AccountStatus.ACTIVE))
                .willReturn(List.of());

        assertThrows(CustomException.class, () -> accountManagementService.setPrimaryAccount("001123456789"));
    }

    @Test
    @DisplayName("??쒓퀎醫??ㅼ젙 ??곸씠 2嫄댁씠?곸씠硫?INVALID_REQUEST ?덉쇅瑜??섏쭊??")
    void setPrimaryAccountFailsWhenMultipleAccountsMatch() {
        Account first = Account.builder()
                .userId(1L)
                .bankCode("001")
                .bankName("BANK")
                .accountNumber("001123456789")
                .balance(1000L)
                .linkedAt(LocalDateTime.now())
                .lastSyncedAt(LocalDateTime.now())
                .isDeleted(Boolean.FALSE)
                .status(AccountStatus.ACTIVE)
                .build();
        Account second = Account.builder()
                .userId(1L)
                .bankCode("020")
                .bankName("OTHER")
                .accountNumber("001123456789")
                .balance(2000L)
                .linkedAt(LocalDateTime.now())
                .lastSyncedAt(LocalDateTime.now())
                .isDeleted(Boolean.FALSE)
                .status(AccountStatus.ACTIVE)
                .build();
        given(currentUserProvider.getCurrentUserId()).willReturn(1L);
        given(accountRepository.findAllByUserIdAndAccountNumberAndStatusAndIsDeletedFalse(1L, "001123456789", AccountStatus.ACTIVE))
                .willReturn(List.of(first, second));

        CustomException exception = assertThrows(
                CustomException.class,
                () -> accountManagementService.setPrimaryAccount("001123456789")
        );

        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.INVALID_REQUEST);
    }
}
